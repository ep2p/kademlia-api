package io.ep2p.kademlia.v4.message;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

import java.io.Serializable;

public class PingKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, String> {
    public final static String TYPE = "PING";
    public PingKademliaMessage() {
        super(TYPE);
        setData("PING");
    }
}
