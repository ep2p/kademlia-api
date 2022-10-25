package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;

public class KeepNodeOnFrontRoutingTableDecorator<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> extends RoutingTableDecorator<ID, C, B> {

    private final KademliaNodeAPI<ID, C> kademliaNodeAPI;

    public KeepNodeOnFrontRoutingTableDecorator(RoutingTable<ID, C, B> routingTable, KademliaNodeAPI<ID, C> kademliaNodeAPI) {
        super(routingTable);
        this.kademliaNodeAPI = kademliaNodeAPI;
    }

    @Override
    public synchronized boolean update(Node<ID, C> node) throws FullBucketException {
        boolean result = super.update(node);
        super.update(kademliaNodeAPI);
        return result;
    }
}
