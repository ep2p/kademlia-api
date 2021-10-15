package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

public class GeneralResponseMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        if (!message.isAlive()){
            kademliaNode.getRoutingTable().delete(message.getNode());
            return (O) new EmptyKademliaMessage<ID, C>();
        }
        return doHandle(kademliaNode, message);
    }

    @SuppressWarnings("unchecked")
    protected <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O doHandle(KademliaNodeAPI<ID,C> kademliaNode, I message) {
        return (O) new EmptyKademliaMessage<ID, C>();
    }


}
