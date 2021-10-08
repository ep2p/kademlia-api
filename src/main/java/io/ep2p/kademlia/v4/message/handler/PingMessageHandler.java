package io.ep2p.kademlia.v4.message.handler;

import io.ep2p.kademlia.v4.exception.FullBucketException;
import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.message.PingKademliaMessage;
import io.ep2p.kademlia.v4.message.PongKademliaMessage;
import io.ep2p.kademlia.v4.node.KademliaNodeAPI;

public class PingMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {

    @Override
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PingKademliaMessage<ID, C>) message);
    }

    protected PongKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, PingKademliaMessage<ID, C> message){
        try {
            kademliaNode.getRoutingTable().update(message.getNode());
        } catch (FullBucketException e) {
            //TODO: log
        }
        return new PongKademliaMessage<ID, C>();
    }
}
