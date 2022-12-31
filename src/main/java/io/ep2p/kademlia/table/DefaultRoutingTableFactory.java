package io.ep2p.kademlia.table;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;

import java.math.BigInteger;

public class DefaultRoutingTableFactory<I extends Number, C extends ConnectionInfo, B extends Bucket<I, C>> implements RoutingTableFactory<I, C, B> {
    private final NodeSettings nodeSettings;

    public DefaultRoutingTableFactory() {
        this(NodeSettings.Default.build());
    }

    public DefaultRoutingTableFactory(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RoutingTable<I, C, B> getRoutingTable(I i) {
        if (i instanceof BigInteger){
            return (RoutingTable<I, C, B>) new BigIntegerRoutingTable<>((BigInteger) i, nodeSettings);
        }else if (i instanceof Long){
            return (RoutingTable<I, C, B>) new LongRoutingTable<>((Long) i, nodeSettings);
        }else if (i instanceof Integer){
            return (RoutingTable<I, C, B>) new IntegerRoutingTable<>((Integer) i, nodeSettings);
        }
        throw new IllegalArgumentException("Unsupported I type");
    }
}
