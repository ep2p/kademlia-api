package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.DHTKey;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class DHTLookupResultKademliaMessage<ID extends Number, C extends ConnectionInfo, K extends DHTKey<ID>, V extends Serializable> extends KademliaMessage<ID, C, DHTLookupResultKademliaMessage.DHTLookupResult<ID, K, V>> {

    public DHTLookupResultKademliaMessage(DHTLookupResult<ID, K, V> data) {
        this();
        setData(data);
    }

    public DHTLookupResultKademliaMessage() {
        super(MessageType.DHT_LOOKUP_RESULT);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DHTLookupResult<ID extends Number, K extends DHTKey<ID>, V extends Serializable> implements Serializable{
        private LookupAnswer.Result result;
        private K key;
        private V value;
    }

}
