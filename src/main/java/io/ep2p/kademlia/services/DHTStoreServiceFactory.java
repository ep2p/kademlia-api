package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DHTStoreServiceFactory<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> {
    PushingDHTStoreService<ID, C, K, V> getDhtStoreService(DHTKademliaNodeAPI<ID, C, K, V> kademliaNodeAPI);

    class DefaultDHTStoreServiceFactory<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements DHTStoreServiceFactory<ID, C, K, V> {
        private final ExecutorService handlerExecutorService;

        public DefaultDHTStoreServiceFactory(ExecutorService handlerExecutorService) {
            this.handlerExecutorService = handlerExecutorService;
        }

        public DefaultDHTStoreServiceFactory() {
            this(Executors.newSingleThreadExecutor());
        }

        @Override
        public PushingDHTStoreService<ID, C, K, V> getDhtStoreService(DHTKademliaNodeAPI<ID, C, K, V> kademliaNodeAPI) {
            return new PushingDHTStoreService<>(kademliaNodeAPI, handlerExecutorService);
        }
    }
}
