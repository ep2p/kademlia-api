package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.message.*;
import io.ep2p.kademlia.util.DateUtil;
import io.ep2p.kademlia.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static io.ep2p.kademlia.protocol.MessageType.DHT_STORE_PULL;


@Slf4j
public class PullingDHTStoreService<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends PushingDHTStoreService<ID, C, K, V> {

    public PullingDHTStoreService(
            DHTKademliaNodeAPI<ID, C, K, V> dhtKademliaNode,
            ExecutorService executorService
    ) {
        super(dhtKademliaNode, executorService);
        this.handlerMapping.put(DHT_STORE_PULL, (kademliaNodeAPI, message) -> {
            if (!(message instanceof DHTStorePullKademliaMessage))
                throw new IllegalArgumentException("Cant handle message. Required: DHTStorePullKademliaMessage");
            return handlePullStore((DHTStorePullKademliaMessage<ID, C, K>) message);
        });
    }

    public Future<StoreAnswer<ID, C, K>> store(K key, @Nullable V value) {
        this.dhtKademliaNode.getKademliaRepository().store(key, value);
        CompletableFuture<StoreAnswer<ID, C, K>> completableFuture = (CompletableFuture<StoreAnswer<ID, C, K>>) super.store(key, null);
        completableFuture.whenComplete((a, t) -> {
            if ((a != null && a.getResult().equals(StoreAnswer.Result.FAILED)) || t != null){
                this.dhtKademliaNode.getKademliaRepository().remove(key);
            }
        });
        return completableFuture;
    }

    protected StoreAnswer<ID, C, K> handleStore(Node<ID, C> caller, Node<ID, C> requester, K key, @Nullable V value){
        StoreAnswer<ID, C, K> storeAnswer;
        ID hash = this.dhtKademliaNode.getKeyHashGenerator().generateHash(key);

        // If some other node is calling the store, and that other node is not this node,
        // But the origin request is by this node, then persist it or just return `STORED` result if its pulling (value = null).
        // The closest node we know to the key knows us as the closest know to the key and not themselves (?!?)
        // Useful only in case of nodeSettings.isEnabledFirstStoreRequestForcePass()
        if (!caller.getId().equals(this.dhtKademliaNode.getId()) && requester.getId().equals(this.dhtKademliaNode.getId())){
            return doStore(requester, key, value);
        }

        // If current node should persist the data, do it immediately
        // For smaller networks this helps to avoid the process of finding alive close nodes to pass data to

        FindNodeAnswer<ID, C> findNodeAnswer = this.dhtKademliaNode.getRoutingTable().findClosest(hash);
        storeAnswer = storeDataToClosestNode(caller, requester, findNodeAnswer.getNodes(), key, value);

        if(storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
            storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.STORED, this.dhtKademliaNode);
        }
        return storeAnswer;
    }

    protected StoreAnswer<ID, C, K> pullAndStore(Node<ID, C> pullFrom, K key){
        KademliaMessage<ID, C, ? extends Serializable> kademliaMessage = this.dhtKademliaNode.getMessageSender().sendMessage(
                this.dhtKademliaNode,
                pullFrom,
                new DHTStorePullKademliaMessage<>(
                        new DHTStorePullKademliaMessage.DHTStorePullData<>(key)
                )
        );
        if (kademliaMessage instanceof DHTLookupResultKademliaMessage){
            DHTLookupResultKademliaMessage<ID, C, K, V> dhtLookupResultKademliaMessage = (DHTLookupResultKademliaMessage<ID, C, K, V>) kademliaMessage;
            if (dhtLookupResultKademliaMessage.getData().getResult().equals(LookupAnswer.Result.FOUND)) {
                return this.doStore(key, dhtLookupResultKademliaMessage.getData().getValue());
            }
        }
        return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, this.dhtKademliaNode);
    }

    protected StoreAnswer<ID, C, K> doStore(Node<ID, C> requester, K key, V value){
        if (value != null)
            return doStore(key, value);
        else {
            return this.pullAndStore(requester, key);
        }
    }

    protected StoreAnswer<ID, C, K> storeDataToClosestNode(Node<ID, C> caller, Node<ID, C> requester, List<ExternalNode<ID, C>> externalNodeList, K key, V value){
        Date date = DateUtil.getDateOfSecondsAgo(this.dhtKademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : externalNodeList) {
            //if current node is the closest node, store the value (Scenario A)
            if(externalNode.getId().equals(this.dhtKademliaNode.getId())){
                return doStore(requester, key, value);
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
                KademliaMessage<ID, C, Serializable> response = this.dhtKademliaNode.getMessageSender().sendMessage(
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

    protected void finalizeStoreResult(K key, StoreAnswer.Result result, Node<ID, C> node) {
        CompletableFuture<StoreAnswer<ID, C, K>> completableFuture = this.storeFutureMap.get(key);
        if (completableFuture != null){
            if (!node.getId().equals(this.dhtKademliaNode.getId()) && result.equals(StoreAnswer.Result.STORED)){
                this.dhtKademliaNode.getKademliaRepository().remove(key);
            }
            completableFuture.complete(getNewStoreAnswer(key, result, node));
        }
    }

    protected DHTLookupResultKademliaMessage<ID, C, K, V> handlePullStore(DHTStorePullKademliaMessage<ID, C, K> dhtStorePullKademliaMessage){
        K key = dhtStorePullKademliaMessage.getData().getKey();
        if (!this.dhtKademliaNode.getKademliaRepository().contains(key)) {
            return new DHTLookupResultKademliaMessage<>(new DHTLookupResultKademliaMessage.DHTLookupResult<>(LookupAnswer.Result.FAILED, key, null));
        }
        return new DHTLookupResultKademliaMessage<>(new DHTLookupResultKademliaMessage.DHTLookupResult<>(LookupAnswer.Result.FOUND, key, this.dhtKademliaNode.getKademliaRepository().get(key)));
    }

    protected EmptyKademliaMessage<ID, C> handleStoreRequest(DHTStoreKademliaMessage<ID,C,K,V> dhtStoreKademliaMessage){
        DHTStoreKademliaMessage.DHTData<ID, C, K, V> data = dhtStoreKademliaMessage.getData();
        if (data.getRequester().getId().equals(this.dhtKademliaNode.getId())){
            finalizeStoreResult(data.getKey(), StoreAnswer.Result.STORED, this.dhtKademliaNode);
            return new EmptyKademliaMessage<>();
        }
        return super.handleStoreRequest(dhtStoreKademliaMessage);
    }

    @Override
    public List<String> getMessageHandlerTypes() {
        List<String> messageHandlerTypes = super.getMessageHandlerTypes();
        messageHandlerTypes.add(DHT_STORE_PULL);
        return messageHandlerTypes;
    }
}
