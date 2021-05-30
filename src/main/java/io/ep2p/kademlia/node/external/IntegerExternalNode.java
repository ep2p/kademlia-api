package io.ep2p.kademlia.node.external;

import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.connection.ConnectionInfo;

/**
 * External node with ID of type Integer
 * @param <C> Your implementation of connection info
 */
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
