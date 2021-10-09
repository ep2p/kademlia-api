package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class PongKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, PongKademliaMessage.PongData> {
    public PongKademliaMessage() {
        super(MessageType.PING);
    }

    public PongKademliaMessage(PongData pongData) {
        this();
        setData(pongData);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PongData implements Serializable {
        private boolean isAlive;
    }
}
