package io.ep2p.kademlia.v4.table;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

public interface RoutingTableFactory<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> {
    RoutingTable<ID, C, B> getRoutingTable(ID i);
}
