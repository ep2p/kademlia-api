package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;

public class NodeDecorator<I extends Number, C extends ConnectionInfo> implements Node<I, C> {
    protected final Node<I, C> node;

    public NodeDecorator(Node<I, C> node) {
        this.node = node;
    }

    @Override
    public C getConnectionInfo() {
        return this.node.getConnectionInfo();
    }

    @Override
    public I getId() {
        return this.node.getId();
    }

    @Override
    public String toString() {
        return "NodeDecorator{" +
                "node=" + node +
                '}';
    }
}
