package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.FindNodeResponseMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import lombok.var;

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
                    // ignore self
                    if (externalNode.getId().equals(kademliaNode.getId())){
                        return;
                    }
                    try {
                        var response = kademliaNode.getMessageSender().sendMessage(kademliaNode, externalNode, new PingKademliaMessage<>());
                        kademliaNode.onMessage(response);
                    } catch (HandlerNotFoundException e) {
                        e.printStackTrace();
                        // TODO
                    } catch (Exception e){
                        e.printStackTrace();
                        // Todo
                    }
                });
            }
        });
        return (O) new EmptyKademliaMessage<ID, C>();
    }
}
