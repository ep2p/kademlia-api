package io.ep2p.kademlia.protocol.message;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.DHTKey;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class DHTStoreResultKademliaMessage<ID extends Number, C extends ConnectionInfo, K extends DHTKey<ID>> extends KademliaMessage<ID, C, DHTStoreResultKademliaMessage.DHTStoreResult<ID, K>> {

    public DHTStoreResultKademliaMessage(DHTStoreResult<ID, K> data) {
        this();
        setData(data);
    }

    public DHTStoreResultKademliaMessage() {
        super(MessageType.DHT_STORE_RESULT);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DHTStoreResult<ID extends Number, K extends DHTKey<ID>> implements Serializable{
        private K key;
        private StoreAnswer.Result result;
    }

}
