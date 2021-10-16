package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

import java.io.Serializable;

public interface MessageSender<ID extends Number, C extends ConnectionInfo> {

    /**
     * @param caller Caller KademliaNodeAPI of this method
     * @param receiver Node to send the message to
     * @param message Message
     * @param <I> Serializable input type of the message
     * @param <O> Serializable output type of the message
     * @return Output message
     */
    <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, I> sendMessage(
            KademliaNodeAPI<ID, C> caller,
            Node<ID, C> receiver,
            KademliaMessage<ID, C, O> message
    );

    /**
     * Sends message async and doesnt return any response
     * @param caller Caller KademliaNodeAPI of this method
     * @param receiver Node to send the message to
     * @param message Message
     * @param <O> Serializable output type of the message
     * @return Output message
     */
    <O extends Serializable> void sendAsyncMessage(KademliaNodeAPI<ID, C> caller,
                          Node<ID, C> receiver,
                          KademliaMessage<ID, C, O> message);
}
