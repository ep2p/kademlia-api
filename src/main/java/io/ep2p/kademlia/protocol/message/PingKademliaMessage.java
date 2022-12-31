package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.ToString;

@ToString(callSuper = true)
public class PingKademliaMessage<I extends Number, C extends ConnectionInfo> extends KademliaMessage<I, C, String> {
    public PingKademliaMessage() {
        super(MessageType.PING);
        setData("PING");
    }
}
