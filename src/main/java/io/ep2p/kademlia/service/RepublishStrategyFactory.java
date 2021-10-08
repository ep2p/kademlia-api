package io.ep2p.kademlia.service;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

public class RepublishStrategyFactory {
    public static Provider PROVIDER = new DefaultProvider();

    public static <ID extends Number, C extends ConnectionInfo, K, V> RepublishStrategy<ID, C, K, V> getRepublishStrategy(){
        return PROVIDER.provide();
    }

    public interface Provider {
        <ID extends Number, C extends ConnectionInfo, K, V> RepublishStrategy<ID, C, K, V> provide();
    }

    public static class DefaultProvider implements RepublishStrategyFactory.Provider {

        @Override
        public <ID extends Number, C extends ConnectionInfo, K, V> RepublishStrategy<ID, C, K, V> provide() {
            return new DefaultRepublishStrategy<>();
        }
    }

}
