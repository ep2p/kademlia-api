package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DHTStoreServiceFactory<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> {
    PushingDHTStoreService<I, C, K, V> getDhtStoreService(DHTKademliaNodeAPI<I, C, K, V> kademliaNodeAPI);

    class DefaultDHTStoreServiceFactory<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements DHTStoreServiceFactory<I, C, K, V> {
        private final ExecutorService handlerExecutorService;

        public DefaultDHTStoreServiceFactory(ExecutorService handlerExecutorService) {
            this.handlerExecutorService = handlerExecutorService;
        }

        public DefaultDHTStoreServiceFactory() {
            this(Executors.newSingleThreadExecutor());
        }

        @Override
        public PushingDHTStoreService<I, C, K, V> getDhtStoreService(DHTKademliaNodeAPI<I, C, K, V> kademliaNodeAPI) {
            return new PushingDHTStoreService<>(kademliaNodeAPI, handlerExecutorService);
        }
    }
}
