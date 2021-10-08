package io.ep2p.kademlia.v4.message;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

public class PongKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, String> {
    public final static String TYPE = "PONG";
    public PongKademliaMessage() {
        super(TYPE);
        setData("PONG");
    }
}
