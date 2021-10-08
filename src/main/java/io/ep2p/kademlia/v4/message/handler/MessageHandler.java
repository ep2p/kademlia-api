package io.ep2p.kademlia.v4.message.handler;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.node.KademliaNodeAPI;

public interface MessageHandler<ID extends Number, C extends ConnectionInfo> {
    <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(
            KademliaNodeAPI<ID, C> kademliaNode,
            I message
    );
}
