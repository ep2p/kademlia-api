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
import io.ep2p.kademlia.services.DHTLookupServiceAPI;
import io.ep2p.kademlia.services.DHTStoreService;
import io.ep2p.kademlia.services.DHTStoreServiceAPI;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@ToString(callSuper = true)
public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNode<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V> {
    @Getter
    private final transient KademliaRepository<K, V> kademliaRepository;
    @Getter
    private final transient KeyHashGenerator<ID, K> keyHashGenerator;
    @Getter
    private transient DHTStoreServiceAPI<ID, C, K, V> storeService = null;
    @Getter
    private transient DHTLookupServiceAPI<ID, C, K, V> lookupService = null;

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        this(id, connectionInfo, routingTable, messageSender, nodeSettings, kademliaRepository, keyHashGenerator, Executors.newFixedThreadPool(nodeSettings.getDhtExecutorPoolSize() + 1), Executors.newScheduledThreadPool(nodeSettings.getScheduledExecutorPoolSize()));
    }

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        super(id, connectionInfo, routingTable, messageSender, nodeSettings,
                                scheduledExecutorService);
        this.kademliaRepository = kademliaRepository;
        this.keyHashGenerator = keyHashGenerator;
        this.initDHTKademliaNode();
    }

    @Override
    public void stop() {
        this.cleanup();
        super.stop();
    }

    @Override
    public void stopNow() {
        this.cleanup();
        super.stopNow();
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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        setLookupService(new DHTLookupService<>(this, getExecutorService()));
        setStoreService(new DHTStoreService<>(this, getExecutorService()));
    }

    public void setStoreService(DHTStoreServiceAPI<ID, C, K, V> storeService) {
        this.storeService = storeService;
        this.registerMessageHandler(MessageType.DHT_STORE, this.storeService);
        this.registerMessageHandler(MessageType.DHT_STORE_RESULT, this.storeService);
    }

    public void setLookupService(DHTLookupServiceAPI<ID, C, K, V> lookupService) {
        this.lookupService = lookupService;
        this.registerMessageHandler(MessageType.DHT_LOOKUP, this.lookupService);
        this.registerMessageHandler(MessageType.DHT_LOOKUP_RESULT, this.lookupService);
    }

}
