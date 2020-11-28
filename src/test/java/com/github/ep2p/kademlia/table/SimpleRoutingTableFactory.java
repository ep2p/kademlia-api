package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;

public class SimpleRoutingTableFactory implements RoutingTableFactory<Integer,EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> {
    @Override
    public RoutingTable<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> getRoutingTable(Integer i) {
        return new IntegerRoutingTable<EmptyConnectionInfo>(i);
    }
}
