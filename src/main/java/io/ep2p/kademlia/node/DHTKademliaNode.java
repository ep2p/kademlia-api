package io.ep2p.kademlia.node;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNode<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V>, MessageHandler<ID, C> {
    private final Map<K, StoreAnswer<ID, K>> storeMap = new ConcurrentHashMap<>();
    private final Map<K, Future<LookupAnswer<ID, K, V>>> lookupFutureMap = new ConcurrentHashMap<>();
    private final Map<K, LookupAnswer<ID, K, V>> lookupAnswerMap = new ConcurrentHashMap<>();
    @Getter
    private final transient KademliaRepository<K, V> kademliaRepository;
    @Getter
    private final transient KeyHashGenerator<ID, K> keyHashGenerator;
    private final transient ExecutorService cleanupExecutor = Executors.newSingleThreadExecutor();

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        this(id, connectionInfo, routingTable, messageSender, nodeSettings, kademliaRepository, keyHashGenerator, Executors.newFixedThreadPool(nodeSettings.getDhtExecutorPoolSize() + 1), Executors.newScheduledThreadPool(nodeSettings.getDhtScheduledExecutorPoolSize()));
    }

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        super(id, connectionInfo, routingTable, messageSender, nodeSettings,
                executorService instanceof ListeningExecutorService ? executorService : MoreExecutors.listeningDecorator(executorService),
                scheduledExecutorService);
        this.kademliaRepository = kademliaRepository;
        this.keyHashGenerator = keyHashGenerator;
        this.initDHTKademliaNode();
    }

    @Override
    public void stop() {
        this.cleanup();
        super.stop();
        this.cleanupExecutor.shutdown();
    }

    @Override
    public void stopNow() {
        this.cleanup();
        super.stopNow();
        this.cleanupExecutor.shutdownNow();
    }

    protected void cleanup(){
        this.storeMap.clear();
        this.lookupFutureMap.clear();
        this.lookupAnswerMap.clear();
    }

    @Override
    public Future<StoreAnswer<ID, K>> store(K key, V value) throws DuplicateStoreRequest {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");

        synchronized (this){
            if (storeMap.containsKey(key)) {
                throw new DuplicateStoreRequest();
            }
        }

        final DHTKademliaNode<ID, C, K, V> self = this;

        ListenableFuture<StoreAnswer<ID, K>> futureAnswer = this.getListeningExecutorService().submit(
                () -> {
                    StoreAnswer<ID, K> storeAnswer = handleStore(self, self, key, value);
                    if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED) || storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
                        return storeAnswer;
                    }
                    storeMap.put(key, storeAnswer);
                    storeAnswer.watch();
                    return storeAnswer;
                });

        futureAnswer.addListener(() -> {
            StoreAnswer<ID, K> storeAnswer = storeMap.remove(key);
            if (storeAnswer != null){
                storeAnswer.finishWatch();
            }
        }, this.cleanupExecutor);

        return futureAnswer;
    }

    @Override
    public Future<LookupAnswer<ID, K, V>> lookup(K key) {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");

        synchronized (this) {
            Future<LookupAnswer<ID, K, V>> f = null;
            if ((f = lookupFutureMap.get(key)) != null) {
                return f;
            }
        }

        DHTKademliaNode<ID, C, K, V> self = this;

        ListenableFuture<LookupAnswer<ID, K, V>> futureAnswer = this.getListeningExecutorService().submit(
                () -> {
                    LookupAnswer<ID, K, V> lookupAnswer = handleLookup(self, self, key, 0);
                    if (lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND) || lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED)){
                        return lookupAnswer;
                    }
                    lookupAnswerMap.put(key, lookupAnswer);
                    lookupAnswer.watch();
                    return lookupAnswer;
                });
        this.lookupFutureMap.put(key, futureAnswer);

        futureAnswer.addListener(() -> {
            this.lookupFutureMap.remove(key);
            this.lookupAnswerMap.remove(key);
        }, this.cleanupExecutor);

        return futureAnswer;
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

        //Otherwise, ask closest node we know to key
        return getDataFromClosestNodes(caller, requester, key, currentTry);
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
        LookupAnswer<ID, K, V> lookupAnswer = null;
        ID hash = hash(key);
        FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(hash);
        Date date = DateUtil.getDateOfSecondsAgo(this.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : findNodeAnswer.getNodes()) {
            //ignore self because we already checked if current node holds the data or not
            //Also ignore nodeToIgnore if its not null
            if(externalNode.getId().equals(getId()) || (caller != null && externalNode.getId().equals(caller.getId())))
                continue;

            //if node is alive, ask for data
            if(recentlySeenOrAlive(externalNode, date)){
                KademliaMessage<ID, C, Serializable> response = getMessageSender().sendMessage(
                        this,
                        externalNode,
                        new DHTLookupKademliaMessage<>(
                                new DHTLookupKademliaMessage.DHTLookup<>(requester, key, currentTry + 1)
                        )
                );
                if (response.isAlive()){
                    return getNewLookupAnswer(key, LookupAnswer.Result.PASSED, this, null);
                }
            }
        }

        return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this, null);

    }

    protected StoreAnswer<ID, K> storeDataToClosestNode(Node<ID, C> caller, Node<ID, C> requester, List<ExternalNode<ID, C>> externalNodeList, K key, V value){
        Date date = DateUtil.getDateOfSecondsAgo(this.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : externalNodeList) {
            //if current node is closest node, store the value (Scenario A)
            if(externalNode.getId().equals(getId())){
                kademliaRepository.store(key, value);
                return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
            }

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

            // otherwise, try next closest requester in routing table
            // if external node is alive, tell it to store the data
            // to know if its alive the last seen should either be close or we ping and check the result
            if(recentlySeenOrAlive(externalNode, date)){
                KademliaMessage<ID, C, Serializable> response = getMessageSender().sendMessage(
                        this,
                        externalNode,
                        new DHTStoreKademliaMessage<>(
                                new DHTStoreKademliaMessage.DHTData<>(requester, key, value)
                        )
                );
                if (response.isAlive()){
                    return getNewStoreAnswer(key, StoreAnswer.Result.PASSED, requester);
                }
            }

        }
        return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, requester);
    }

    /**
     * Check if a node is seen recently or is alive
     * @param externalNode Node to check
     * @param date Date to use for comparing
     * @return boolean True if node is alive
     */
    private boolean recentlySeenOrAlive(ExternalNode<ID, C> externalNode, Date date) {
        if (externalNode.getLastSeen().after(date))
            return true;
        KademliaMessage<ID, C, ?> pingAnswer = getMessageSender().sendMessage(this, externalNode, new PingKademliaMessage<>());
        if (!pingAnswer.isAlive()){
            try {
                onMessage(pingAnswer);
            } catch (HandlerNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
        return pingAnswer.isAlive();
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
                if (!(message instanceof DHTStoreKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTStoreKademliaMessage");
                return (O) handleStoreRequest((DHTStoreKademliaMessage<ID, C, K, V>) message);
            case MessageType.DHT_STORE_RESULT:
                if (!(message instanceof DHTStoreResultKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTStoreResultKademliaMessage");
                return (O) handleStoreResult((DHTStoreResultKademliaMessage<ID, C, K>) message);
            case MessageType.DHT_LOOKUP:
                if (!(message instanceof DHTLookupKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTLookupKademliaMessage");
                return (O) handleLookupRequest((DHTLookupKademliaMessage<ID, C, K>) message);
            case MessageType.DHT_LOOKUP_RESULT:
                if (!(message instanceof DHTLookupResultKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTLookupResultKademliaMessage");
                return (O) handleLookupResult((DHTLookupResultKademliaMessage<ID, C, K, V>) message);
            default:
                throw new IllegalArgumentException("message param is not supported");
        }

    }

    protected KademliaMessage<ID, C, ?> handleLookupResult(DHTLookupResultKademliaMessage<ID, C, K, V> message) {
        DHTLookupResultKademliaMessage.DHTLookupResult<K, V> data = message.getData();
        LookupAnswer<ID, K, V> answer = this.lookupAnswerMap.get(data.getKey());
        if (answer != null){
            answer.setResult(data.getResult());
            answer.setKey(data.getKey());
            answer.setValue(data.getValue());
            answer.setNodeId(message.getNode().getId());
            answer.finishWatch();
        }
        return new EmptyKademliaMessage<>();
    }

    protected KademliaMessage<ID, C, ?> handleLookupRequest(DHTLookupKademliaMessage<ID, C, K> message) {
        final KademliaNodeAPI<ID, C> caller = this;
        this.getExecutorService().submit(() -> {
            DHTLookupKademliaMessage.DHTLookup<ID, C, K> data = message.getData();
            LookupAnswer<ID, K, V> lookupAnswer = handleLookup(caller, data.getRequester(), data.getKey(), data.getCurrentTry());
            if (lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED) || lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND)){
                getMessageSender().sendAsyncMessage(caller, data.getRequester(), new DHTLookupResultKademliaMessage<>(
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
        StoreAnswer<ID, K> storeAnswer = this.storeMap.get(data.getKey());
        if (storeAnswer != null){
            storeAnswer.setNodeId(message.getNode().getId());
            storeAnswer.setResult(data.getResult());
            storeAnswer.setAlive(true);
            storeAnswer.finishWatch();
        }
        return new EmptyKademliaMessage<>();
    }

    protected KademliaMessage<ID, C, ?> handleStoreRequest(DHTStoreKademliaMessage<ID,C,K,V> dhtStoreKademliaMessage){
        final KademliaNodeAPI<ID, C> caller = this;
        this.getExecutorService().submit(() -> {
            DHTStoreKademliaMessage.DHTData<ID, C, K, V> data = dhtStoreKademliaMessage.getData();
            StoreAnswer<ID, K> storeAnswer = handleStore(dhtStoreKademliaMessage.getNode(), data.getRequester(), data.getKey(), data.getValue());
            if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED)) {
                getMessageSender().sendAsyncMessage(
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

    protected ListeningExecutorService getListeningExecutorService(){
        assert this.getExecutorService() instanceof ListeningExecutorService;
        return (ListeningExecutorService) this.getExecutorService();
    }

}
