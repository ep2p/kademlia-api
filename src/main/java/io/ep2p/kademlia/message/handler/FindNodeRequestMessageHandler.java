package io.ep2p.kademlia.message.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.message.FindNodeResponseMessage;
import io.ep2p.kademlia.message.KademliaMessage;
import io.ep2p.kademlia.node.KademliaNodeAPI;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindNodeRequestMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ? extends Serializable>, O extends KademliaMessage<ID, C, ? extends Serializable>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        //TODO
        return (O) new FindNodeResponseMessage<ID, C>();
    }
}