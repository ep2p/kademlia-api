package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.*;
import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.kademlia.util.DateUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNode<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V>, MessageHandler<ID, C> {
    protected final Map<K, CompletableFuture<StoreAnswer<ID, K>>> storeMap = new ConcurrentHashMap<>();
    protected final Map<K, CompletableFuture<LookupAnswer<ID, K, V>>> lookupMap = new ConcurrentHashMap<>();
    @Getter
    private final KademliaRepository<K, V> kademliaRepository;
    @Getter
    private final KeyHashGenerator<ID, K> keyHashGenerator;
    protected final ExecutorService executorService;
    protected final ScheduledExecutorService scheduledExecutor;

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        super(id, connectionInfo, routingTable, messageSender, nodeSettings);
        this.executorService = Executors.newFixedThreadPool(nodeSettings.getDhtExecutorPoolSize());
        this.scheduledExecutor = Executors.newScheduledThreadPool(nodeSettings.getDhtCleanupExecutorPoolSize());
        this.kademliaRepository = kademliaRepository;
        this.keyHashGenerator = keyHashGenerator;
        this.initDHTKademliaNode();
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            this.executorService.shutdown();
            this.scheduledExecutor.shutdown();
        }
        this.storeMap.clear();
        this.lookupMap.clear();
        super.stop();
    }

    @Override
    public void stopNow() {
        if (this.isRunning()) {
            this.executorService.shutdownNow();
            this.scheduledExecutor.shutdownNow();
        }
        this.storeMap.clear();
        this.lookupMap.clear();
        super.stopNow();
    }

    @Override
    public Future<StoreAnswer<ID, K>> store(K key, V value) {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");

        CompletableFuture<StoreAnswer<ID, K>> future = new CompletableFuture<>();

        synchronized (this){
            if (storeMap.containsKey(key)) {
                future.completeExceptionally(new DuplicateStoreRequest());
                return future;
            }

            storeMap.put(key, future);
        }

        executorService.submit(() -> {
            try {
                StoreAnswer<ID, K> storeAnswer = handleStore(this, this, key, value);
                StoreAnswer.Result storeAnswerResult = storeAnswer.getResult();

                // If current node has already stored the data or says storing is failed then complete the future and remove it from map
                if (storeAnswerResult.equals(StoreAnswer.Result.STORED) || storeAnswerResult.equals(StoreAnswer.Result.FAILED)){
                    future.complete(storeAnswer);
                    storeMap.remove(key);
                }else {
                    // Schedule to clean this future no matter how long the caller wants to wait
                    scheduleStoreCleanup(key, future);
                }

            }catch (Throwable ex){
                ex.printStackTrace();
                future.completeExceptionally(ex);
                storeMap.remove(key);
            }
        });

        return future;
    }


    @Override
    public Future<LookupAnswer<ID, K, V>> lookup(K key) {
        CompletableFuture<LookupAnswer<ID, K, V>> future = new CompletableFuture<>();

        synchronized (this){
            if (lookupMap.containsKey(key)) {
                future.completeExceptionally(new DuplicateStoreRequest());
                return future;
            }

            lookupMap.put(key, future);
        }

        executorService.submit(() -> {
            try {
                LookupAnswer<ID, K, V> lookupAnswer = handleLookup(this, this, key, 0);
                LookupAnswer.Result lookupAnswerResult = lookupAnswer.getResult();

                // If current node already knows the value the data or says lookup has failed then complete the future and remove it from map
                if (lookupAnswerResult.equals(LookupAnswer.Result.FOUND) || lookupAnswerResult.equals(LookupAnswer.Result.FAILED)){
                    future.complete(lookupAnswer);
                    lookupMap.remove(key);
                }else {
                    // Schedule to clean this future no matter how long the caller wants to wait
                    scheduleLookupCleanup(key, future);
                }

            }catch (Throwable ex){
                future.completeExceptionally(ex);
                lookupMap.remove(key);
            }
        });

        return future;
    }

    protected LookupAnswer<ID, K, V> handleLookup(Node<ID, C> caller, Node<ID, C> requester, K key, int currentTry){
        // Check if current node contains data
        if(kademliaRepository.contains(key)){
            V value = kademliaRepository.get(key);
            return getNewLookupAnswer(key, LookupAnswer.Result.FOUND, this, value);
        }

        // If max tries has reached then return failed
        if (currentTry == getNodeSettings().getIdentifierSize()){
            return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this, null);
        }

        LookupAnswer<ID, K, V> lookupAnswer;

        //Otherwise, ask closest node we know to key
        lookupAnswer = getDataFromClosestNodes(caller, requester, key, currentTry);

        if(lookupAnswer == null){
            lookupAnswer = getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this, null);
        }

        return lookupAnswer;
    }


    protected StoreAnswer<ID, K> handleStore(Node<ID, C> caller, Node<ID, C> requester, K key, V value){
        StoreAnswer<ID, K> storeAnswer;
        ID hash = hash(key);

        // If some other node is calling the store, and that other node is not this node,
        // but the origin request is by this node, then persist it
        if (caller != null && !caller.getId().equals(getId()) && requester.getId().equals(getId())){
            return doStore(key, value);
        }

        // If current node should persist data, do it immediately
        // For smaller networks this helps avoiding the process of finding alive close nodes to pass data to
        if(getId().equals(hash)) {
            storeAnswer = doStore(key, value);
        } else {
            FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(hash);
            storeAnswer = storeDataToClosestNode(caller, requester, findNodeAnswer.getNodes(), key, value);
        }

        if(storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
            storeAnswer = doStore(key, value);
        }
        return storeAnswer;
    }


    protected ID hash(K key){
        return keyHashGenerator.generateHash(key);
    }

    protected LookupAnswer<ID, K, V> getDataFromClosestNodes(Node<ID, C> caller, Node<ID, C> requester, K key, int currentTry){
        LookupAnswer<ID, K, V> getAnswer = null;
        ID hash = hash(key);
        FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(hash);
        Date date = DateUtil.getDateOfSecondsAgo(this.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : findNodeAnswer.getNodes()) {
            //ignore self because we already checked if current node holds the data or not
            //Also ignore nodeToIgnore if its not null
            if(externalNode.getId().equals(getId()) || (caller != null && externalNode.getId().equals(caller.getId())))
                continue;


            KademliaMessage<ID, C, ?> pingAnswer;
            //if node is alive, ask for data
            if(externalNode.getLastSeen().before(date) || (pingAnswer = getMessageSender().sendMessage(this, externalNode, new PingKademliaMessage<>())).isAlive()){
                var response = getMessageSender().sendMessage(
                        this,
                        externalNode,
                        new DHTLookupKademliaMessage<>(
                                new DHTLookupKademliaMessage.DHTLookup<>(this, key, currentTry + 1)
                        )
                );
                if (response.isAlive()){
                    getAnswer = getNewLookupAnswer(key, LookupAnswer.Result.PASSED, requester, null);
                    break;
                }

            //otherwise remove the node from routing table, since its offline
            }else if(!pingAnswer.isAlive()){
                getRoutingTable().delete(externalNode);
            }
        }

        return getAnswer;
    }

    protected StoreAnswer<ID, K> storeDataToClosestNode(Node<ID, C> caller, Node<ID, C> requester, List<ExternalNode<ID, C>> externalNodeList, K key, V value){
        Date date = DateUtil.getDateOfSecondsAgo(this.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        StoreAnswer<ID, K> storeAnswer = null;
        for (ExternalNode<ID, C> externalNode : externalNodeList) {
            //if current node is closest node, store the value (Scenario A)
            if(externalNode.getId().equals(getId())){
                kademliaRepository.store(key, value);
                storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
                break;
            } else {

                // Continue if requester is known to be the closest but its also same as caller
                // This means that this is the first time that PASS is happening, or in other words:
                // This is the first time the request has passed the store request to some other node. So we try more.
                // This approach can be disabled through nodeSettings "Enabled First Store Request Force Pass"
                // This has no conflicts with 'Scenario A' because:
                // If we were the closest node we'd have already stored the data
                if (requester.getId().equals(externalNode.getId()) && requester.getId().equals(caller.getId())
                    && getNodeSettings().isEnabledFirstStoreRequestForcePass()
                ){
                    continue;
                }

                //otherwise try next close requester in routing table
                KademliaMessage<ID, C, ?> pingAnswer;
                //if external node is alive, tell it to store the data
                // To know if its alive the last seen should either be close or we ping and check the result
                if(externalNode.getLastSeen().after(date) || (pingAnswer = getMessageSender().sendMessage(this, externalNode, new PingKademliaMessage<>())).isAlive()){
                    var response = getMessageSender().sendMessage(
                            this,
                            externalNode,
                            new DHTStoreKademliaMessage<>(
                                    new DHTStoreKademliaMessage.DHTData<>(requester, key, value)
                            )
                    );
                    if (response.isAlive()){
                        storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.PASSED, requester);
                        break;
                    }
                }else {
                    // We have definitely pinged the node, lets handle the pong specially now that node is offline
                    try {
                        onMessage(pingAnswer);
                    } catch (HandlerNotFoundException e) {
                        // Should not get stuck here. Main objective is to store the message
                        log.error(e.getMessage(), e);
                    }
                }
            }

        }
        if (storeAnswer == null)
            storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.FAILED, requester);
        return storeAnswer;
    }


    // ****    PROTOCOL METHODS HERE    **** //
    // Handling incoming DHT related messages//


    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        if (message.isAlive()){
            getRoutingTable().forceUpdate(message.getNode());
        }
        switch (message.getType()) {
            case MessageType.DHT_STORE:
                assert message instanceof DHTStoreKademliaMessage;
                return (O) handleStoreRequest((DHTStoreKademliaMessage<ID, C, K, V>) message);
            case MessageType.DHT_STORE_RESULT:
                assert message instanceof DHTStoreResultKademliaMessage;
                return (O) handleStoreResult((DHTStoreResultKademliaMessage<ID, C, K>) message);
            case MessageType.DHT_LOOKUP:
                assert message instanceof DHTLookupKademliaMessage;
                return (O) handleLookupRequest((DHTLookupKademliaMessage<ID, C, K>) message);
            case MessageType.DHT_LOOKUP_RESULT:
                assert message instanceof DHTLookupResultKademliaMessage;
                return (O) handleLookupResult((DHTLookupResultKademliaMessage<ID, C, K, V>) message);
        }

        throw new IllegalArgumentException("message param is not supported");
    }

    protected KademliaMessage<ID, C, ?> handleLookupResult(DHTLookupResultKademliaMessage<ID, C, K, V> message) {
        var data = message.getData();
        var future = this.lookupMap.get(data.getKey());
        if (future != null){
            future.complete(getNewLookupAnswer(data.getKey(), data.getResult(), message.getNode(), data.getValue()));
            this.lookupMap.remove(data.getKey());
        }
        return new EmptyKademliaMessage<>();
    }

    protected KademliaMessage<ID, C, ?> handleLookupRequest(DHTLookupKademliaMessage<ID, C, K> message) {
        final KademliaNodeAPI<ID, C> caller = this;
        executorService.submit(() -> {
            var data = message.getData();
            var lookupAnswer = handleLookup(caller, data.getRequester(), data.getKey(), data.getCurrentTry());
            if (lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED) || lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND)){
                getMessageSender().sendMessage(caller, data.getRequester(), new DHTLookupResultKademliaMessage<>(
                        new DHTLookupResultKademliaMessage.DHTLookupResult<>(
                                lookupAnswer.getResult(),
                                data.getKey(),
                                lookupAnswer.getValue()
                        )
                ));
            }
        });

        return new EmptyKademliaMessage<>();
    }

    protected KademliaMessage<ID, C, ?> handleStoreResult(DHTStoreResultKademliaMessage<ID, C, K> message) {
        DHTStoreResultKademliaMessage.DHTStoreResult<K> data = message.getData();
        var future = this.storeMap.get(data.getKey());
        if (future != null){
            future.complete(getNewStoreAnswer(data.getKey(), data.getResult(), message.getNode()));
            this.storeMap.remove(data.getKey());
        }
        return new EmptyKademliaMessage<>();
    }

    protected KademliaMessage<ID, C, ?> handleStoreRequest(DHTStoreKademliaMessage<ID,C,K,V> dhtStoreKademliaMessage){
        final KademliaNodeAPI<ID, C> caller = this;
        executorService.submit(() -> {
            var data = dhtStoreKademliaMessage.getData();
            var storeAnswer = handleStore(dhtStoreKademliaMessage.getNode(), data.getRequester(), data.getKey(), data.getValue());
            if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED)) {
                getMessageSender().sendMessage(
                        caller,
                        data.getRequester(),
                        new DHTStoreResultKademliaMessage<>(
                                new DHTStoreResultKademliaMessage.DHTStoreResult<>(data.getKey(), StoreAnswer.Result.STORED)
                        )
                );
            }
        });
        return new EmptyKademliaMessage<>();
    }


    // **** PROTECTED HELPER METHODS HERE **** //

    protected void scheduleLookupCleanup(K key, CompletableFuture<LookupAnswer<ID,K,V>> future) {
        this.scheduledExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.complete(LookupAnswer.generateWithResult(key, LookupAnswer.Result.TIMEOUT));
                future.cancel(true);
                lookupMap.remove(key);
            }
        }, getNodeSettings().getMaximumStoreAndLookupTimeoutValue(), getNodeSettings().getMaximumStoreAndGetTimeoutTimeUnit());
    }

    protected void scheduleStoreCleanup(K key, CompletableFuture<StoreAnswer<ID, K>> future) {
        this.scheduledExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.complete(StoreAnswer.generateWithResult(key, StoreAnswer.Result.TIMEOUT));
                future.cancel(true);
                storeMap.remove(key);
            }
        }, getNodeSettings().getMaximumStoreAndLookupTimeoutValue(), getNodeSettings().getMaximumStoreAndGetTimeoutTimeUnit());
    }

    protected void initDHTKademliaNode(){
        this.registerMessageHandler(MessageType.DHT_LOOKUP, this);
        this.registerMessageHandler(MessageType.DHT_LOOKUP_RESULT, this);
        this.registerMessageHandler(MessageType.DHT_STORE, this);
        this.registerMessageHandler(MessageType.DHT_STORE_RESULT, this);
    }


    protected StoreAnswer<ID, K> doStore(K key, V value){
        kademliaRepository.store(key, value);
        return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
    }


    // **** HELPER METHODS HERE **** //

    protected StoreAnswer<ID, K> getNewStoreAnswer(K k, StoreAnswer.Result result, Node<ID, C> node){
        StoreAnswer<ID, K> storeAnswer = new StoreAnswer<>();
        storeAnswer.setAlive(true);
        storeAnswer.setNodeId(node.getId());
        storeAnswer.setKey(k);
        storeAnswer.setResult(result);
        return storeAnswer;
    }

    protected LookupAnswer<ID, K, V> getNewLookupAnswer(K k, LookupAnswer.Result result, Node<ID, C> node, @Nullable V value){
        LookupAnswer<ID, K, V> lookupAnswer = new LookupAnswer<>();
        lookupAnswer.setAlive(true);
        lookupAnswer.setNodeId(node.getId());
        lookupAnswer.setKey(k);
        lookupAnswer.setResult(result);
        lookupAnswer.setValue(value);
        return lookupAnswer;
    }

}
