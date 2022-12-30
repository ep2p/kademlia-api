package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.kademlia.services.*;
import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.Future;

public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNodeAPIDecorator<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V>{
    @Getter
    private final KeyHashGenerator<ID, K> keyHashGenerator;
    @Getter
    private final KademliaRepository<K, V> kademliaRepository;
    @Getter
    private transient DHTStoreServiceAPI<ID, C, K, V> storeService;
    @Getter
    private transient DHTLookupServiceAPI<ID, C, K, V> lookupService;
    @Getter
    private final DHTStoreServiceFactory<ID, C, K, V> dhtStoreServiceFactory;
    @Getter
    private final DHTLookupServiceFactory<ID, C, K, V> dhtLookupServiceFactory;

    public DHTKademliaNode(KademliaNodeAPI<ID, C> kademliaNode, KeyHashGenerator<ID, K> keyHashGenerator, KademliaRepository<K, V> kademliaRepository, DHTStoreServiceFactory<ID, C, K, V> dhtStoreServiceFactory, DHTLookupServiceFactory<ID, C, K, V> dhtLookupServiceFactory) {
        super(kademliaNode);
        this.keyHashGenerator = keyHashGenerator;
        this.kademliaRepository = kademliaRepository;
        this.dhtStoreServiceFactory = dhtStoreServiceFactory;
        this.dhtLookupServiceFactory = dhtLookupServiceFactory;
        this.initDHTKademliaNode();
    }

    public DHTKademliaNode(KademliaNodeAPI<ID, C> kademliaNode, KeyHashGenerator<ID, K> keyHashGenerator, KademliaRepository<K, V> kademliaRepository) {
        this(kademliaNode, keyHashGenerator, kademliaRepository, new DHTStoreServiceFactory.DefaultDHTStoreServiceFactory<>(), new DHTLookupServiceFactory.DefaultDHTLookupServiceFactory<>());
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
    public Future<StoreAnswer<ID, C, K>> store(K key, V value) {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");
        return this.storeService.store(key, value);
    }

    @Override
    public Future<LookupAnswer<ID, C, K, V>> lookup(K key) {
        if(!isRunning())
            throw new IllegalStateException("Node is not running");
        return this.lookupService.lookup(key);
    }

    protected void initDHTKademliaNode(){
        setLookupService(this.dhtLookupServiceFactory.getDhtLookupService(this));
        setStoreService(this.dhtStoreServiceFactory.getDhtStoreService(this));
    }

    protected void setStoreService(DHTStoreServiceAPI<ID, C, K, V> storeService) {
        this.storeService = storeService;
        this.storeService.getMessageHandlerTypes().forEach(type -> {
            this.registerMessageHandler(type, this.storeService);
        });
    }

    protected void setLookupService(DHTLookupServiceAPI<ID, C, K, V> lookupService) {
        this.lookupService = lookupService;
        this.lookupService.getMessageHandlerTypes().forEach(type -> {
            this.registerMessageHandler(type, this.lookupService);
        });
    }
}
