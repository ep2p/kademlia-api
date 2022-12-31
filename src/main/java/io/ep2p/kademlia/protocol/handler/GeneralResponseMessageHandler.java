package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

public class GeneralResponseMessageHandler<I extends Number, C extends ConnectionInfo> implements MessageHandler<I, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O handle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        if (!message.isAlive()){
            kademliaNode.getRoutingTable().delete(message.getNode());
            return (O) new EmptyKademliaMessage<I, C>();
        }
        return doHandle(kademliaNode, message);
    }

    @SuppressWarnings("unchecked")
    protected <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O doHandle(KademliaNodeAPI<I,C> kademliaNode, U message) {
        return (O) new EmptyKademliaMessage<I, C>();
    }


}
