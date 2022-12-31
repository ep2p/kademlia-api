package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;

import java.io.Serializable;

public interface MessageSender<I extends Number, C extends ConnectionInfo> {

    /**
     * @param caller Caller KademliaNodeAPI of this method
     * @param receiver Node to send the message to
     * @param message Message
     * @param <U> Serializable input type of the message
     * @param <O> Serializable output type of the message
     * @return Output message
     */
    <U extends Serializable, O extends Serializable> KademliaMessage<I, C, O> sendMessage(
            KademliaNodeAPI<I, C> caller,
            Node<I, C> receiver,
            KademliaMessage<I, C, U> message
    );

    /**
     * Sends message async and doesnt return any response
     * @param caller Caller KademliaNodeAPI of this method
     * @param receiver Node to send the message to
     * @param message Message
     * @param <U> Serializable input type of the message
     */
    <U extends Serializable> void sendAsyncMessage(KademliaNodeAPI<I, C> caller,
                          Node<I, C> receiver,
                          KademliaMessage<I, C, U> message);
}
