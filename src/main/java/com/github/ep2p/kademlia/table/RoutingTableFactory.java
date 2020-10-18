package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;

public interface RoutingTableFactory {
    <C extends ConnectionInfo, I> RoutingTable<C> getRoutingTable(I i);
}
