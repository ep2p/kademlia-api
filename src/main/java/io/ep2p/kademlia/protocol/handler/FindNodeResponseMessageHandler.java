package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FindNodeResponseMessageHandler<I extends Number, C extends ConnectionInfo> implements MessageHandler<I, C> {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O handle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        executorService.submit(() -> ((FindNodeResponseMessage<I, C>) message).getData().getNodes().forEach(externalNode -> {
            // ignore self
            if (externalNode.getId().equals(kademliaNode.getId())){
                return;
            }
            try {
                KademliaMessage<I, C, Serializable> response = kademliaNode.getMessageSender().sendMessage(kademliaNode, externalNode, new PingKademliaMessage<>());
                if (response.isAlive() && kademliaNode.getRoutingTable().update(response.getNode())) {
                    FindNodeRequestMessage<I, C> findNodeRequestMessage = new FindNodeRequestMessage<>();
                    findNodeRequestMessage.setData(kademliaNode.getId());
                    KademliaMessage<I, C, Serializable> findNodeResponse = kademliaNode.getMessageSender().sendMessage(kademliaNode, message.getNode(), findNodeRequestMessage);
                    kademliaNode.onMessage(findNodeResponse);
                }
            } catch (HandlerNotFoundException | FullBucketException e) {
                log.error(e.getMessage(), e);
            }
        }));
        return (O) new EmptyKademliaMessage<I, C>();
    }

}
