package io.ep2p.kademlia.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class PongKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, PongKademliaMessage.PongData> {
    public final static String TYPE = "PONG";
    public PongKademliaMessage() {
        super(TYPE);
    }

    public PongKademliaMessage(PongData pongData) {
        super(TYPE);
        setData(pongData);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PongData implements Serializable {
        private boolean isAlive;
    }
}
