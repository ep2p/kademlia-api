package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;

public interface RoutingTableFactory<I extends Number, C extends ConnectionInfo, B extends Bucket<I, C>> {
    RoutingTable<I, C, B> getRoutingTable(I i);
}
