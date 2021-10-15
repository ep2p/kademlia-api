package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.DHTKey;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class DHTLookupKademliaMessage<ID extends Number, C extends ConnectionInfo, K extends DHTKey<ID>> extends KademliaMessage<ID, C, DHTLookupKademliaMessage.DHTLookup<ID, C, K>> {

    public DHTLookupKademliaMessage(DHTLookup<ID, C, K> data) {
        this();
        setData(data);
    }

    public DHTLookupKademliaMessage() {
        super(MessageType.DHT_LOOKUP);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DHTLookup<ID extends Number, C extends ConnectionInfo, K extends DHTKey<ID>> implements Serializable{
        protected Node<ID, C> requester;
        private K key;
        private int currentTry;
    }

}
