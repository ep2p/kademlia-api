package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.ShutdownKademliaMessage;

public class ShutdownMessageHandler<I extends Number, C extends ConnectionInfo> implements MessageHandler<I, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O handle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        assert message instanceof ShutdownKademliaMessage;
        kademliaNode.getRoutingTable().delete(message.getNode());
        return (O) new EmptyKademliaMessage<I, C>();
    }
}
