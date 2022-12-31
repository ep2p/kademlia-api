package io.ep2p.kademlia.protocol.message;

import com.google.common.base.Objects;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
public class DHTStoreKademliaMessage<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaMessage<I, C, DHTStoreKademliaMessage.DHTData<I, C, K, V>> {

    public DHTStoreKademliaMessage(DHTData<I, C, K, V> data) {
        this();
        setData(data);
    }

    public DHTStoreKademliaMessage() {
        super(MessageType.DHT_STORE);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DHTData<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements Serializable{
        private Node<I, C> requester;
        private K key;
        private V value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DHTData<?, ?, ?, ?> dhtData = (DHTData<?, ?, ?, ?>) o;
            return Objects.equal(getRequester(), dhtData.getRequester()) && Objects.equal(getKey(), dhtData.getKey()) && Objects.equal(getValue(), dhtData.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getRequester(), getKey(), getValue());
        }
    }

}
