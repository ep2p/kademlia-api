package io.ep2p.kademlia.protocol.message;

import com.google.common.base.Objects;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
public class DHTStoreResultKademliaMessage<I extends Number, C extends ConnectionInfo, K extends Serializable> extends KademliaMessage<I, C, DHTStoreResultKademliaMessage.DHTStoreResult<K>> {

    public DHTStoreResultKademliaMessage(DHTStoreResult<K> data) {
        this();
        setData(data);
    }

    public DHTStoreResultKademliaMessage() {
        super(MessageType.DHT_STORE_RESULT);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DHTStoreResult<K extends Serializable> implements Serializable{
        private K key;
        private StoreAnswer.Result result;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DHTStoreResult<?> that = (DHTStoreResult<?>) o;
            return Objects.equal(getKey(), that.getKey()) && getResult() == that.getResult();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey(), getResult());
        }
    }

}
