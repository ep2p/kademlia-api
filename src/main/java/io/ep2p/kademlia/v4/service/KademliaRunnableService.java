package io.ep2p.kademlia.v4.service;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.node.KademliaNode;
import lombok.Getter;

public abstract class KademliaRunnableService<ID extends Number, C extends ConnectionInfo> implements Runnable {
    @Getter
    private final KademliaNode<ID, C> kademliaNode;

    protected KademliaRunnableService(KademliaNode<ID, C> kademliaNode) {
        this.kademliaNode = kademliaNode;
    }
}
