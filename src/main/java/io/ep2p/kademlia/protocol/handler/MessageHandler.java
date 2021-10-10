package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

public interface MessageHandler<ID extends Number, C extends ConnectionInfo> {
    <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(
            KademliaNodeAPI<ID, C> kademliaNode,
            I message
    );
}
