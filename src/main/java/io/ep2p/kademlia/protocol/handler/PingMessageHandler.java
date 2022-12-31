package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.kademlia.protocol.message.PongKademliaMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PingMessageHandler<I extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<I, C> {

    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O doHandle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        return (O) doHandle(kademliaNode, (PingKademliaMessage<I, C>) message);
    }

    protected PongKademliaMessage<I, C> doHandle(KademliaNodeAPI<I, C> kademliaNode, PingKademliaMessage<I, C> message){
        if (kademliaNode.isRunning()){
            try {
                kademliaNode.getRoutingTable().update(message.getNode());
            } catch (FullBucketException e) {
                log.error(e.getMessage(), e);
            }
        }
        PongKademliaMessage<I, C> pongKademliaMessage = new PongKademliaMessage<>();
        pongKademliaMessage.setAlive(kademliaNode.isRunning());
        return pongKademliaMessage;
    }
}
