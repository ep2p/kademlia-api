package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class DHTStoreKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, DHTStoreKademliaMessage.DHTData<?, ?>> {

    public <K extends Serializable, V extends Serializable> DHTStoreKademliaMessage(DHTData<K, V> data) {
        this();
        setData(data);
    }

    public DHTStoreKademliaMessage() {
        super(MessageType.DHT_STORE);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DHTData<K extends Serializable, V extends Serializable> implements Serializable{
        private K key;
        private V value;
    }

}
