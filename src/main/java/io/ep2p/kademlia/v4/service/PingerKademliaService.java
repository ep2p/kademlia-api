package io.ep2p.kademlia.v4.service;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.node.KademliaNode;

public class PingerKademliaService<ID extends Number, C extends ConnectionInfo> extends KademliaRunnableService<ID, C> {

    public PingerKademliaService(KademliaNode<ID, C> kademliaNode) {
        super(kademliaNode);
    }

    //TODO
    @Override
    public void run() {

    }
}
