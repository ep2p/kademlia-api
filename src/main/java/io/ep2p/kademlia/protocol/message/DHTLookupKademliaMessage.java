package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class DHTLookupKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, DHTLookupKademliaMessage.DHTLookup<?, ?>> {

    public <K extends Serializable, V extends Serializable> DHTLookupKademliaMessage(DHTLookup<K, V> data) {
        this();
        setData(data);
    }

    public DHTLookupKademliaMessage() {
        super(MessageType.DHT_LOOKUP);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DHTLookup<K extends Serializable, V extends Serializable> implements Serializable{
        private K key;
    }

}
