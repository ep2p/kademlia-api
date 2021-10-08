package io.ep2p.kademlia.v4.table;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.v4.connection.ConnectionInfo;

import java.math.BigInteger;

public class DefaultRoutingTableFactory<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> implements RoutingTableFactory<ID, C, B> {
    private final NodeSettings nodeSettings;

    public DefaultRoutingTableFactory() {
        this(NodeSettings.Default.build());
    }

    public DefaultRoutingTableFactory(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RoutingTable<ID, C, B> getRoutingTable(ID i) {
        if (i instanceof BigInteger){
            return (RoutingTable<ID, C, B>) new BigIntegerRoutingTable<C>((BigInteger) i, nodeSettings);
        }else if (i instanceof Long){
            return (RoutingTable<ID, C, B>) new LongRoutingTable<C>((Long) i, nodeSettings);
        }else if (i instanceof Integer){
            return (RoutingTable<ID, C, B>) new IntegerRoutingTable<C>((Integer) i, nodeSettings);
        }
        throw new IllegalArgumentException("Unsupported ID type");
    }
}
