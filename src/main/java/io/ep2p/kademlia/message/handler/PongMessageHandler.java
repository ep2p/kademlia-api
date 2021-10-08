package io.ep2p.kademlia.message.handler;

import io.ep2p.kademlia.message.EmptyKademliaMessage;
import io.ep2p.kademlia.message.PongKademliaMessage;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.message.KademliaMessage;
import io.ep2p.kademlia.node.KademliaNodeAPI;

public class PongMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PongKademliaMessage<ID, C>) message);
    }

    protected EmptyKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, PongKademliaMessage<ID, C> message){
        try {
            kademliaNode.getRoutingTable().update(message.getNode());
        } catch (FullBucketException e) {
            //TODO: log
        }
        return new EmptyKademliaMessage<ID, C>();
    }
}
