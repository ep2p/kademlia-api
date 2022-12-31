package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DHTLookupServiceFactory<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> {
    DHTLookupServiceAPI<I, C, K, V> getDhtLookupService(DHTKademliaNodeAPI<I, C, K, V> kademliaNodeAPI);

    class DefaultDHTLookupServiceFactory<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements DHTLookupServiceFactory<I, C, K, V> {
        private final ExecutorService handlerExecutorService;

        public DefaultDHTLookupServiceFactory(ExecutorService handlerExecutorService) {
            this.handlerExecutorService = handlerExecutorService;
        }

        public DefaultDHTLookupServiceFactory() {
            this(Executors.newSingleThreadExecutor());
        }

        @Override
        public DHTLookupServiceAPI<I, C, K, V> getDhtLookupService(DHTKademliaNodeAPI<I, C, K, V> kademliaNodeAPI) {
            return new DHTLookupService<>(kademliaNodeAPI, handlerExecutorService);
        }
    }
}
