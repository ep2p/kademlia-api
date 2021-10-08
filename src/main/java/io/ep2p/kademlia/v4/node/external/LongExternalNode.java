package io.ep2p.kademlia.v4.node.external;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.node.Node;

/**
 * External node with ID of type Long
 * @param <C> Your implementation of connection info
 */
public class LongExternalNode<C extends ConnectionInfo> extends ExternalNode<Long, C> {

    public LongExternalNode(Node<Long, C> node, Long distance) {
        super(node, distance);
    }

    @Override
    public int compareTo(Object o) {
        ExternalNode<Long, C> c = (ExternalNode<Long, C>) o;
        return Long.compare(distance, c.distance);
    }
}
