package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

import java.io.Serializable;

public interface MessageHandler<I extends Number, C extends ConnectionInfo> {
    <U extends KademliaMessage<I, C, ? extends Serializable>, O extends KademliaMessage<I, C, ? extends Serializable>> O handle(
            KademliaNodeAPI<I, C> kademliaNode,
            U message
    );
}
