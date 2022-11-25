package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.ToString;

@ToString
public class FindNodeRequestMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, ID> {
    public FindNodeRequestMessage() {
        super(MessageType.FIND_NODE_REQ);
    }

    public ID getDestinationId(){
        return this.getData();
    }
}
