package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;

public class SimpleRoutingTableFactory implements RoutingTableFactory<EmptyConnectionInfo, Integer> {

    @Override
    public RoutingTable<EmptyConnectionInfo> getRoutingTable(Integer id) {
        return new RoutingTable<EmptyConnectionInfo>(id);
    }
}
