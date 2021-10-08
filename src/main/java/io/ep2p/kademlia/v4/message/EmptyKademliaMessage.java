package io.ep2p.kademlia.v4.message;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

import java.io.Serializable;

public class EmptyKademliaMessage <ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, Serializable> {
    public final static String TYPE = "EMPTY_MESSAGE";
    public EmptyKademliaMessage() {
        super(TYPE);
    }
}
