package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class DHTStoreKademliaMessage<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaMessage<ID, C, DHTStoreKademliaMessage.DHTData<ID, C, K, V>> {

    public DHTStoreKademliaMessage(DHTData<ID, C, K, V> data) {
        this();
        setData(data);
    }

    public DHTStoreKademliaMessage() {
        super(MessageType.DHT_STORE);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DHTData<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements Serializable{
        private Node<ID, C> requester;
        private K key;
        private V value;
    }

}
