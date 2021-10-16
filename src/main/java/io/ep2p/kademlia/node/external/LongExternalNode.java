package io.ep2p.kademlia.node.external;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import org.jetbrains.annotations.NotNull;

/**
 * External node with ID of type Long
 * @param <C> Your implementation of connection info
 */
public class LongExternalNode<C extends ConnectionInfo> extends ExternalNode<Long, C> {

    public LongExternalNode(Node<Long, C> node, Long distance) {
        super(node, distance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(@NotNull Object o) {
        ExternalNode<Long, C> c = (ExternalNode<Long, C>) o;
        return Long.compare(distance, c.distance);
    }
}
