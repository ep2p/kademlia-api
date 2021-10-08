package io.ep2p.kademlia.v4.message.handler;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.message.EmptyKademliaMessage;
import io.ep2p.kademlia.v4.message.FindNodeResponseMessage;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.message.PingKademliaMessage;
import io.ep2p.kademlia.v4.node.KademliaNodeAPI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindNodeResponseMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                ((FindNodeResponseMessage<ID, C>) message).getData().getNodes().forEach(externalNode -> {
                    kademliaNode.getMessageSender().sendMessage(kademliaNode, externalNode, new PingKademliaMessage<>());
                });
            }
        });
        return (O) new EmptyKademliaMessage<ID, C>();
    }
}
