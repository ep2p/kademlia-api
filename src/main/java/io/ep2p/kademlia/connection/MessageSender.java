package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

import java.io.Serializable;

public interface MessageSender<ID extends Number, C extends ConnectionInfo> {
    <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, I> sendMessage(
            KademliaNodeAPI<ID, C> caller,
            Node<ID, C> receiver,
            KademliaMessage<ID, C, O> message
    );
}
