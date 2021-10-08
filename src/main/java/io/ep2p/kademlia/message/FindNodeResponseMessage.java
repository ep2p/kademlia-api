package io.ep2p.kademlia.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;

public class FindNodeResponseMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, FindNodeAnswer<ID, C>> {
    public final static String TYPE = "FIND_NODE_RESPONSE";

    public FindNodeResponseMessage() {
        super(TYPE);
    }
}
