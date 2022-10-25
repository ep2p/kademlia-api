package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.kademlia.services.DHTLookupService;
import io.ep2p.kademlia.services.DHTStoreService;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNode<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V> {
    @Getter
    private final transient KademliaRepository<K, V> kademliaRepository;
    @Getter
    private final transient KeyHashGenerator<ID, K> keyHashGenerator;
    private final transient ExecutorService cleanupExecutor = Executors.newSingleThreadExecutor();
    private transient DHTStoreService<ID, C, K, V> storeService = null;
    private transient DHTLookupService<ID, C, K, V> lookupService = null;

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        this(id, connectionInfo, routingTable, messageSender, nodeSettings, kademliaRepository, keyHashGenerator, Executors.newFixedThreadPool(nodeSettings.getDhtExecutorPoolSize() + 1), Executors.newScheduledThreadPool(nodeSettings.getScheduledExecutorPoolSize()));
    }

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        super(id, connectionInfo, routingTable, messageSender, nodeSettings,
                executorService,
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
        if (this.storeService != null)
            this.storeService.cleanUp();
        if (this.lookupService != null)
            this.lookupService.cleanUp();
    }

    @Override
    public Future<StoreAnswer<ID, K>> store(K key, V value) throws DuplicateStoreRequest {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");
        return this.storeService.store(key, value);
    }

    @Override
    public Future<LookupAnswer<ID, K, V>> lookup(K key) {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");

        return this.lookupService.lookup(key);
    }

    protected void initDHTKademliaNode(){
        this.storeService = new DHTStoreService<>(this, getExecutorService(), this.cleanupExecutor);
        this.lookupService = new DHTLookupService<>(this, getExecutorService(), this.cleanupExecutor);
        this.registerMessageHandler(MessageType.DHT_LOOKUP, this.lookupService);
        this.registerMessageHandler(MessageType.DHT_LOOKUP_RESULT, this.lookupService);
        this.registerMessageHandler(MessageType.DHT_STORE, this.storeService);
        this.registerMessageHandler(MessageType.DHT_STORE_RESULT, this.storeService);
    }
}
