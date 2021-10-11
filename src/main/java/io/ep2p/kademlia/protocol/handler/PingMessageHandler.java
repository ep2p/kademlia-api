package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.kademlia.protocol.message.PongKademliaMessage;

public class PingMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O doHandle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PingKademliaMessage<ID, C>) message);
    }

    protected PongKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, PingKademliaMessage<ID, C> message){
        if (kademliaNode.isRunning()){
            try {
                kademliaNode.getRoutingTable().update(message.getNode());
            } catch (FullBucketException e) {
                e.printStackTrace();
                //TODO: log
            }
        }
        PongKademliaMessage<ID, C> pongKademliaMessage = new PongKademliaMessage<>();
        pongKademliaMessage.setAlive(kademliaNode.isRunning());
        return pongKademliaMessage;
    }
}
