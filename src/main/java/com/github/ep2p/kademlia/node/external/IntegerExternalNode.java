package com.github.ep2p.kademlia.node.external;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

public class IntegerExternalNode<C extends ConnectionInfo> extends ExternalNode<Integer, C> {

    public IntegerExternalNode() {
    }

    public IntegerExternalNode(Node<Integer, C> node, Integer distance) {
        super(node, distance);
    }

    @Override
    public int compareTo(Object o) {
        ExternalNode<Integer, C> c = (ExternalNode<Integer, C>) o;
        return Integer.compare(distance, c.distance);
    }
}
