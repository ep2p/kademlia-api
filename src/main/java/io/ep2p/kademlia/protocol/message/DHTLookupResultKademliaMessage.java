package io.ep2p.kademlia.protocol.message;

import com.google.common.base.Objects;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
public class DHTLookupResultKademliaMessage<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaMessage<I, C, DHTLookupResultKademliaMessage.DHTLookupResult<K, V>> {

    public DHTLookupResultKademliaMessage(DHTLookupResult<K, V> data) {
        this();
        setData(data);
    }

    public DHTLookupResultKademliaMessage() {
        super(MessageType.DHT_LOOKUP_RESULT);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DHTLookupResult<K extends Serializable, V extends Serializable> implements Serializable{
        private LookupAnswer.Result result;
        private K key;
        private V value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DHTLookupResult<?, ?> that = (DHTLookupResult<?, ?>) o;
            return getResult() == that.getResult() && Objects.equal(getKey(), that.getKey()) && Objects.equal(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getResult(), getKey(), getValue());
        }
    }

}
