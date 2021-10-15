package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.*;

public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNode<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V>, MessageHandler<ID, C> {
    protected Map<K, Future<StoreAnswer<ID, K>>> storeMap = new ConcurrentHashMap<>();
    protected Map<K, Future<LookupAnswer<ID, K, V>>> lookupMap = new ConcurrentHashMap<>();
    protected final ExecutorService executorService;
    protected final ScheduledExecutorService scheduledExecutor;

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings) {
        super(id, connectionInfo, routingTable, messageSender, nodeSettings);
        this.executorService = Executors.newFixedThreadPool(nodeSettings.getStoreRequestPoolSize());
        this.scheduledExecutor = Executors.newScheduledThreadPool(nodeSettings.getStoreAndGetCleanupPoolSize());
        this.initDHTKademliaNode();
    }

    @Override
    public Future<StoreAnswer<ID, K>> store(K key, V value) {
        CompletableFuture<StoreAnswer<ID, K>> future = new CompletableFuture<>();

        if (storeMap.containsKey(key)) {
            future.completeExceptionally(new DuplicateStoreRequest());
            return future;
        }

        storeMap.put(key, future);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    StoreAnswer<ID, K> storeAnswer = doStore(key, value);
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
                    future.completeExceptionally(ex);
                    storeMap.remove(key);
                }
            }
        });

        return future;
    }


    @Override
    public Future<LookupAnswer<ID, K, V>> lookup(K key) {
        CompletableFuture<LookupAnswer<ID, K, V>> future = new CompletableFuture<>();

        if (lookupMap.containsKey(key)) {
            future.completeExceptionally(new DuplicateStoreRequest());
            return future;
        }

        lookupMap.put(key, future);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    LookupAnswer<ID, K, V> lookupAnswer = doLookup(key);
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
            }
        });

        return future;
    }


    // TODO
    @Override
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return null;
    }

    // TODO
    protected StoreAnswer<ID, K> doStore(K key, V value){
        return null;
    }

    // TODO
    protected LookupAnswer<ID, K, V> doLookup(K key){
        return null;
    }



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

}
