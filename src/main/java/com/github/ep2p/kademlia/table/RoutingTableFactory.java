package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;

public interface RoutingTableFactory<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> {
    RoutingTable<ID, C, B> getRoutingTable(ID i);
}
