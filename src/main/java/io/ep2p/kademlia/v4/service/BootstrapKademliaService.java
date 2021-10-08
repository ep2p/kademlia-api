package io.ep2p.kademlia.v4.service;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.node.KademliaNode;
import io.ep2p.kademlia.v4.node.Node;

public class BootstrapKademliaService<ID extends Number, C extends ConnectionInfo> extends KademliaRunnableService<ID, C> {
    private final Node<ID, C> bootstrapNode;

    public BootstrapKademliaService(KademliaNode<ID, C> kademliaNode, Node<ID, C> bootstrapNode) {
        super(kademliaNode);
        this.bootstrapNode = bootstrapNode;
    }

    //TODO
    @Override
    public void run() {

    }
}
