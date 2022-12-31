package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
public class EmptyKademliaMessage<I extends Number, C extends ConnectionInfo> extends KademliaMessage<I, C, Serializable> {
    public EmptyKademliaMessage() {
        super(MessageType.EMPTY);
    }
}
