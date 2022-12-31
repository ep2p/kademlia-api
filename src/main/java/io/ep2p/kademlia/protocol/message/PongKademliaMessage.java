package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
public class PongKademliaMessage<I extends Number, C extends ConnectionInfo> extends KademliaMessage<I, C, String> {
    @Getter
    @Setter
    private boolean fromFindHandler = false;

    public PongKademliaMessage() {
        super(MessageType.PONG);
        setData("PONG");
    }
}
