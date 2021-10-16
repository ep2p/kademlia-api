package io.ep2p.kademlia.node.external;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * External node with ID of type BigInteger
 * @param <C> Your implementation of connection info
 */
public class BigIntegerExternalNode<C extends ConnectionInfo> extends ExternalNode<BigInteger, C> {

    public BigIntegerExternalNode(Node<BigInteger, C> node, BigInteger distance) {
        super(node, distance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(@NotNull Object o) {
        ExternalNode<BigInteger, C> c = (ExternalNode<BigInteger, C>) o;
        return distance.compareTo(c.distance);
    }
}
