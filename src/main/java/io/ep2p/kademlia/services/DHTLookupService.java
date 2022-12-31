package io.ep2p.kademlia.services;


import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.message.DHTLookupKademliaMessage;
import io.ep2p.kademlia.protocol.message.DHTLookupResultKademliaMessage;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.util.DateUtil;
import io.ep2p.kademlia.util.NodeUtil;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class DHTLookupService<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements DHTLookupServiceAPI<I, C, K, V> {
    private final Map<K, List<CompletableFuture<LookupAnswer<I, C, K, V>>>> lookupFutureMap = new ConcurrentHashMap<>();

    private final DHTKademliaNodeAPI<I, C, K, V> dhtKademliaNode;
    private final ExecutorService handlerExecutorService;

    public DHTLookupService(
            DHTKademliaNodeAPI<I, C, K, V> dhtKademliaNode,
            ExecutorService executorService
    ) {
        this.dhtKademliaNode = dhtKademliaNode;
        this.handlerExecutorService = executorService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O handle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        if (message.isAlive()){
            this.dhtKademliaNode.getRoutingTable().forceUpdate(message.getNode());
        }
        switch (message.getType()) {
            case MessageType.DHT_LOOKUP:
                if (!(message instanceof DHTLookupKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTLookupKademliaMessage");
                return (O) handleLookupRequest((DHTLookupKademliaMessage<I, C, K>) message);
            case MessageType.DHT_LOOKUP_RESULT:
                if (!(message instanceof DHTLookupResultKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTLookupResultKademliaMessage");
                return (O) handleLookupResult((DHTLookupResultKademliaMessage<I, C, K, V>) message);
            default:
                throw new IllegalArgumentException("message param is not supported");
        }
    }

    public void cleanUp(){
        this.lookupFutureMap.forEach((k, completableFutures) -> completableFutures.forEach(lookupAnswerCompletableFuture -> lookupAnswerCompletableFuture.cancel(true)));
        this.lookupFutureMap.clear();
    }


    public Future<LookupAnswer<I, C, K, V>> lookup(K key){
        List<CompletableFuture<LookupAnswer<I, C, K, V>>> futures = this.lookupFutureMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());

        CompletableFuture<LookupAnswer<I, C, K, V>> lookupAnswerFuture = new CompletableFuture<>();
        lookupAnswerFuture.whenComplete((a, t) -> futures.remove(lookupAnswerFuture));

        futures.add(lookupAnswerFuture);

        this.handlerExecutorService.submit(() -> {
            LookupAnswer<I, C, K, V> lookupAnswer = handleLookup(this.dhtKademliaNode, this.dhtKademliaNode, key, 0);
            if (lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND) || lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED)) {
                lookupAnswerFuture.complete(lookupAnswer);
                futures.remove(lookupAnswerFuture);
            }
        });

        return lookupAnswerFuture;
    }

    protected LookupAnswer<I, C, K, V> handleLookup(Node<I, C> caller, Node<I, C> requester, K key, int currentTry){
        // Check if current node contains data
        if(this.dhtKademliaNode.getKademliaRepository().contains(key)){
            V value = this.dhtKademliaNode.getKademliaRepository().get(key);
            return getNewLookupAnswer(key, LookupAnswer.Result.FOUND, this.dhtKademliaNode, value);
        }

        // If max tries has reached then return failed
        if (currentTry == this.dhtKademliaNode.getNodeSettings().getIdentifierSize()){
            return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this.dhtKademliaNode, null);
        }

        //Otherwise, ask the closest node we know to key
        return getDataFromClosestNodes(caller, requester, key, currentTry);
    }

    protected LookupAnswer<I, C, K, V> getDataFromClosestNodes(Node<I, C> caller, Node<I, C> requester, K key, int currentTry){
        I hash = this.dhtKademliaNode.getKeyHashGenerator().generateHash(key);
        FindNodeAnswer<I, C> findNodeAnswer = this.dhtKademliaNode.getRoutingTable().findClosest(hash);
        Date date = DateUtil.getDateOfSecondsAgo(this.dhtKademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<I, C> externalNode : findNodeAnswer.getNodes()) {
            //ignore self because we already checked if current node holds the data or not
            //Also ignore nodeToIgnore if its not null
            if(externalNode.getId().equals(this.dhtKademliaNode.getId()) || (caller != null && externalNode.getId().equals(caller.getId())))
                continue;

            // If requester knew the data, it wouldn't have asked for it
            if (externalNode.getId().equals(requester.getId())){
                continue;
            }

            //if node is alive, ask for data
            if(NodeUtil.recentlySeenOrAlive(this.dhtKademliaNode, externalNode, date)){
                KademliaMessage<I, C, Serializable> response = this.dhtKademliaNode.getMessageSender().sendMessage(
                        this.dhtKademliaNode,
                        externalNode,
                        new DHTLookupKademliaMessage<>(
                                new DHTLookupKademliaMessage.DHTLookup<>(requester, key, currentTry + 1)
                        )
                );
                if (response.isAlive()){
                    return getNewLookupAnswer(key, LookupAnswer.Result.PASSED, this.dhtKademliaNode, null);
                }
            }
        }

        return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this.dhtKademliaNode, null);

    }

    protected EmptyKademliaMessage<I, C> handleLookupResult(DHTLookupResultKademliaMessage<I, C, K, V> message) {
        DHTLookupResultKademliaMessage.DHTLookupResult<K, V> data = message.getData();
        List<CompletableFuture<LookupAnswer<I, C, K, V>>> futuresList = this.lookupFutureMap.get(data.getKey());
        if (futuresList != null){
            LookupAnswer<I, C, K, V> answer = new LookupAnswer<>();
            answer.setResult(data.getResult());
            answer.setKey(data.getKey());
            answer.setValue(data.getValue());
            answer.setNode(message.getNode());
            for (CompletableFuture<LookupAnswer<I, C, K, V>> future : futuresList) {
                future.complete(answer);
            }
        }
        return new EmptyKademliaMessage<>();
    }

    protected EmptyKademliaMessage<I, C> handleLookupRequest(DHTLookupKademliaMessage<I, C, K> message) {
        this.handlerExecutorService.submit(() -> {
            DHTLookupKademliaMessage.DHTLookup<I, C, K> data = message.getData();
            LookupAnswer<I, C, K, V> lookupAnswer = handleLookup(this.dhtKademliaNode, data.getRequester(), data.getKey(), data.getCurrentTry());
            if (lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED) || lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND)){
                this.dhtKademliaNode.getMessageSender().sendAsyncMessage(this.dhtKademliaNode, data.getRequester(), new DHTLookupResultKademliaMessage<>(
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

    protected LookupAnswer<I, C, K, V> getNewLookupAnswer(K k, LookupAnswer.Result result, Node<I, C> node, @Nullable V value){
        LookupAnswer<I, C, K, V> lookupAnswer = new LookupAnswer<>();
        lookupAnswer.setAlive(true);
        lookupAnswer.setNode(node);
        lookupAnswer.setKey(k);
        lookupAnswer.setResult(result);
        lookupAnswer.setValue(value);
        return lookupAnswer;
    }
}
