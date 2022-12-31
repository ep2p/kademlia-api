package io.ep2p.kademlia.protocol.message;

import com.google.common.base.Objects;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public abstract class KademliaMessage<I extends Number, C extends ConnectionInfo, D extends Serializable> {
    private D data;
    private String type;
    private Node<I, C> node;
    private boolean isAlive = true;

    public KademliaMessage() {
    }

    protected KademliaMessage(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KademliaMessage<?, ?, ?> that = (KademliaMessage<?, ?, ?>) o;
        return Objects.equal(getData(), that.getData()) && Objects.equal(getType(), that.getType()) && Objects.equal(getNode(), that.getNode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getData(), getType(), getNode());
    }
}
