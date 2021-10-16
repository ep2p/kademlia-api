package io.ep2p.kademlia.node.external;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * External node with ID of type Integer
 * @param <C> Your implementation of connection info
 */
public class IntegerExternalNode<C extends ConnectionInfo> extends ExternalNode<Integer, C> {

    public IntegerExternalNode(Node<Integer, C> node, Integer distance) {
        super(node, distance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(@NotNull Object o) {
        ExternalNode<Integer, C> c = (ExternalNode<Integer, C>) o;
        return Integer.compare(distance, c.distance);
    }
}
