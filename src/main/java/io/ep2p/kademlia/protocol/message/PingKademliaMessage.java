package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;

public class PingKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, String> {
    public final static String TYPE = "PING";
    public PingKademliaMessage() {
        super(TYPE);
        setData("PING");
    }
}
