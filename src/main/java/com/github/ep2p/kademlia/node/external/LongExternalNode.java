package com.github.ep2p.kademlia.node.external;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

/**
 * @brief External node with ID of type Long
 * @param <C> Your implementation of connection info
 */
public class LongExternalNode<C extends ConnectionInfo> extends ExternalNode<Long, C> {

    public LongExternalNode() {
    }

    public LongExternalNode(Node<Long, C> node, Long distance) {
        super(node, distance);
    }

    @Override
    public int compareTo(Object o) {
        ExternalNode<Long, C> c = (ExternalNode<Long, C>) o;
        return Long.compare(distance, c.distance);
    }
}
