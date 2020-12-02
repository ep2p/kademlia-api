package com.github.ep2p.kademlia.node.external;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

import java.math.BigInteger;

public class BigIntegerExternalNode<C extends ConnectionInfo> extends ExternalNode<BigInteger, C> {

    public BigIntegerExternalNode() {
    }

    public BigIntegerExternalNode(Node<BigInteger, C> node, BigInteger distance) {
        super(node, distance);
    }

    @Override
    public int compareTo(Object o) {
        ExternalNode<BigInteger, C> c = (ExternalNode<BigInteger, C>) o;
        return distance.compareTo(c.distance);
    }
}
