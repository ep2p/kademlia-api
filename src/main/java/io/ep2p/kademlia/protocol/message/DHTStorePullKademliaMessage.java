package io.ep2p.kademlia.protocol.message;

import com.google.common.base.Objects;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
public class DHTStorePullKademliaMessage<I extends Number, C extends ConnectionInfo, K extends Serializable> extends KademliaMessage<I, C, DHTStorePullKademliaMessage.DHTStorePullData<K>> {

    public DHTStorePullKademliaMessage(DHTStorePullData<K> data) {
        this();
        setData(data);
    }

    public DHTStorePullKademliaMessage() {
        super(MessageType.DHT_STORE_PULL);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DHTStorePullData<K extends Serializable> implements Serializable{
        private K key;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DHTStorePullData<?> dhtLookup = (DHTStorePullData<?>) o;
            return Objects.equal(getKey(), dhtLookup.getKey());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey());
        }
    }

}
