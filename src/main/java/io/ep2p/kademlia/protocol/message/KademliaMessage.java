package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class KademliaMessage<ID extends Number, C extends ConnectionInfo, D extends Serializable> {
    private D data;
    private final String type;
    private Node<ID, C> node;

    protected KademliaMessage(String type) {
        this.type = type;
    }
}
