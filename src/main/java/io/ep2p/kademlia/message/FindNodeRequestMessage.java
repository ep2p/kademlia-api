package io.ep2p.kademlia.message;

import io.ep2p.kademlia.connection.ConnectionInfo;

public class FindNodeRequestMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, ID> {
    public final static String TYPE = "FIND_NODE";

    public FindNodeRequestMessage() {
        super(TYPE);
    }

    public ID getDestinationId(){
        return this.getData();
    }
}
