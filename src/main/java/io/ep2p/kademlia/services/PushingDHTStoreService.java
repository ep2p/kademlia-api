package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.message.DHTStoreKademliaMessage;
import io.ep2p.kademlia.protocol.message.DHTStoreResultKademliaMessage;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.util.DateUtil;
import io.ep2p.kademlia.util.NodeUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;


public class PushingDHTStoreService<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements DHTStoreServiceAPI<I, C, K, V> {
    protected final DHTKademliaNodeAPI<I, C, K, V> dhtKademliaNode;
    protected final ExecutorService handlerExecutorService;
    protected final Map<K, CompletableFuture<StoreAnswer<I, C, K>>> storeFutureMap = new ConcurrentHashMap<>();
    protected final Map<String, BiFunction<KademliaNodeAPI<I, C>, KademliaMessage<I, C, ? extends Serializable>, KademliaMessage<I, C, ? extends Serializable>>> handlerMapping = new HashMap<>();

    @SuppressWarnings("unchecked")
    public PushingDHTStoreService(
            DHTKademliaNodeAPI<I, C, K, V> dhtKademliaNode,
            ExecutorService executorService
    ) {
        this.dhtKademliaNode = dhtKademliaNode;
        this.handlerExecutorService = executorService;
        this.handlerMapping.put(MessageType.DHT_STORE, (kademliaNodeAPI, message) -> {
            if (!(message instanceof DHTStoreKademliaMessage))
                throw new IllegalArgumentException("Cant handle message. Required: DHTStoreKademliaMessage");
            return handleStoreRequest((DHTStoreKademliaMessage<I, C, K, V>) message);
        });
        this.handlerMapping.put(MessageType.DHT_STORE_RESULT, (kademliaNodeAPI, message) -> {
            if (!(message instanceof DHTStoreResultKademliaMessage))
                throw new IllegalArgumentException("Cant handle message. Required: DHTStoreResultKademliaMessage");
            return handleStoreResult((DHTStoreResultKademliaMessage<I, C, K>) message);
        });
    }

    public Future<StoreAnswer<I, C, K>> store(K key, V value) {
        CompletableFuture<StoreAnswer<I, C, K>> completableFuture = new CompletableFuture<>();
        storeFutureMap.computeIfAbsent(key, k -> {
            completableFuture.whenComplete((a, t) -> storeFutureMap.remove(key));
            StoreAnswer<I, C, K> storeAnswer = handleStore(this.dhtKademliaNode, this.dhtKademliaNode, key, value);
            if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED) || storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
                completableFuture.complete(storeAnswer);
                return null;
            }
            return completableFuture;
        });
        return completableFuture;
    }

    public void cleanUp(){
        this.storeFutureMap.forEach((k, storeAnswerCompletableFuture) -> storeAnswerCompletableFuture.cancel(true));
        this.storeFutureMap.clear();
    }

    protected StoreAnswer<I, C, K> handleStore(Node<I, C> caller, Node<I, C> requester, K key, V value){
        StoreAnswer<I, C, K> storeAnswer;
        I hash = this.dhtKademliaNode.getKeyHashGenerator().generateHash(key);

        // If some other node is calling the store, and that other node is not this node,
        // But the origin request is by this node, then persist it
        // The closest node we know to the key knows us as the closest know to the key and not themselves (?!?)
        // Useful only in case of nodeSettings.isEnabledFirstStoreRequestForcePass()
        if (!caller.getId().equals(this.dhtKademliaNode.getId()) && requester.getId().equals(this.dhtKademliaNode.getId())){
            return doStore(key, value);
        }

        // If current node should persist the data, do it immediately
        // For smaller networks this helps to avoid the process of finding alive close nodes to pass data to

        FindNodeAnswer<I, C> findNodeAnswer = this.dhtKademliaNode.getRoutingTable().findClosest(hash);
        storeAnswer = storeDataToClosestNode(caller, requester, findNodeAnswer.getNodes(), key, value);

        if(storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
            storeAnswer = doStore(key, value);
        }
        return storeAnswer;
    }

    protected StoreAnswer<I, C, K> doStore(K key, V value){
        this.dhtKademliaNode.getKademliaRepository().store(key, value);
        return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this.dhtKademliaNode);
    }

    protected StoreAnswer<I, C, K> storeDataToClosestNode(Node<I, C> caller, Node<I, C> requester, List<ExternalNode<I, C>> externalNodeList, K key, V value){
        Date date = DateUtil.getDateOfSecondsAgo(this.dhtKademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<I, C> externalNode : externalNodeList) {
            //if current node is the closest node, store the value (Scenario A)
            if(externalNode.getId().equals(this.dhtKademliaNode.getId())){
                return doStore(key, value);
            }

            // Continue if requester is known to be the closest, but it's also same as caller
            // This means that this is the first time that PASS is happening and requester wants for force pushing it to other nodes,
            // or in other words:
            // This is the first time the requester node has passed the store request to some other node. So we try more.
            // This approach can be disabled through nodeSettings "Enabled First Store Request Force Pass"
            // This has no conflicts with 'Scenario A' because:
            // If we were the closest node we'd have already stored the data

            if (requester.getId().equals(externalNode.getId()) && requester.getId().equals(caller.getId())
                    && this.dhtKademliaNode.getNodeSettings().isEnabledFirstStoreRequestForcePass()
            ){
                continue;
            }

            // otherwise, try next closest node in routing table
            // if close node is alive, tell it to store the data
            // to know if it's alive the last seen should either be close or we ping and check the result
            if(NodeUtil.recentlySeenOrAlive(this.dhtKademliaNode, externalNode, date)){
                KademliaMessage<I, C, Serializable> response = this.dhtKademliaNode.getMessageSender().sendMessage(
                        this.dhtKademliaNode,
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


    protected void finalizeStoreResult(K key, StoreAnswer.Result result, Node<I, C> node) {
        CompletableFuture<StoreAnswer<I, C, K>> completableFuture = this.storeFutureMap.get(key);
        if (completableFuture != null){
            completableFuture.complete(getNewStoreAnswer(key, result, node));
        }
    }

    protected EmptyKademliaMessage<I, C> handleStoreResult(DHTStoreResultKademliaMessage<I, C, K> message) {
        DHTStoreResultKademliaMessage.DHTStoreResult<K> data = message.getData();
        this.finalizeStoreResult(data.getKey(), data.getResult(), message.getNode());
        return new EmptyKademliaMessage<>();
    }

    protected EmptyKademliaMessage<I, C> handleStoreRequest(DHTStoreKademliaMessage<I,C,K,V> dhtStoreKademliaMessage){
        this.handlerExecutorService.submit(() -> {
            DHTStoreKademliaMessage.DHTData<I, C, K, V> data = dhtStoreKademliaMessage.getData();
            StoreAnswer<I, C, K> storeAnswer = handleStore(dhtStoreKademliaMessage.getNode(), data.getRequester(), data.getKey(), data.getValue());
            if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED)) {
                this.dhtKademliaNode.getMessageSender().sendAsyncMessage(
                        this.dhtKademliaNode,
                        data.getRequester(),
                        new DHTStoreResultKademliaMessage<>(
                                new DHTStoreResultKademliaMessage.DHTStoreResult<>(data.getKey(), StoreAnswer.Result.STORED)
                        )
                );
            }
        });
        return new EmptyKademliaMessage<>();
    }


    protected StoreAnswer<I, C, K> getNewStoreAnswer(K k, StoreAnswer.Result result, Node<I, C> node){
        StoreAnswer<I, C, K> storeAnswer = new StoreAnswer<>();
        storeAnswer.setAlive(true);
        storeAnswer.setNode(node);
        storeAnswer.setKey(k);
        storeAnswer.setResult(result);
        return storeAnswer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O handle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        if (message.isAlive()){
            this.dhtKademliaNode.getRoutingTable().forceUpdate(message.getNode());
        }
        BiFunction<KademliaNodeAPI<I, C>, KademliaMessage<I, C, ? extends Serializable>, KademliaMessage<I, C, ? extends Serializable>> biFunction = this.handlerMapping.get(message.getType());
        if (biFunction == null){
            throw new IllegalArgumentException("Message param is not supported");
        }
        return (O) biFunction.apply(kademliaNode, message);
    }
}
