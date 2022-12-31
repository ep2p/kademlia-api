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
public class DHTLookupKademliaMessage<I extends Number, C extends ConnectionInfo, K extends Serializable> extends KademliaMessage<I, C, DHTLookupKademliaMessage.DHTLookup<I, C, K>> {

    public DHTLookupKademliaMessage(DHTLookup<I, C, K> data) {
        this();
        setData(data);
    }

    public DHTLookupKademliaMessage() {
        super(MessageType.DHT_LOOKUP);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DHTLookup<I extends Number, C extends ConnectionInfo, K extends Serializable> implements Serializable{
        private Node<I, C> requester;
        private K key;
        private int currentTry;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DHTLookup<?, ?, ?> dhtLookup = (DHTLookup<?, ?, ?>) o;
            return getCurrentTry() == dhtLookup.getCurrentTry() && Objects.equal(getRequester(), dhtLookup.getRequester()) && Objects.equal(getKey(), dhtLookup.getKey());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getRequester(), getKey(), getCurrentTry());
        }
    }

}
