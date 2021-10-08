package io.ep2p.kademlia.v4.message.handler;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.node.KademliaNodeAPI;

import java.io.Serializable;

public interface MessageHandler<ID extends Number, C extends ConnectionInfo> {
    <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, O> handle(
            KademliaNodeAPI<ID, C> kademliaNode,
            KademliaMessage<ID, C, I> message
    );
}
