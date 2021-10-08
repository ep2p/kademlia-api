package io.ep2p.kademlia.v4.connection;

import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.node.KademliaNodeAPI;
import io.ep2p.kademlia.v4.node.Node;

import java.io.Serializable;

public interface MessageSender<ID extends Number, C extends ConnectionInfo> {
    <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, I> sendMessage(
            KademliaNodeAPI<ID, C> caller,
            Node<ID, C> receiver,
            KademliaMessage<ID, C, O> message
    );
}
