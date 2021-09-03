package io.ep2p.kademlia.service;

import io.ep2p.kademlia.connection.ConnectionInfo;

public class RepublishStrategyFactory {

    public static <ID extends Number, C extends ConnectionInfo, K, V> RepublishStrategy<ID, C, K, V> getRepublishStrategy(){
        return new DefaultRepublishStrategy<>();
    }

}
