package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Node<C extends ConnectionInfo> implements Serializable {
    protected int id;
    protected C connectionInfo;
    protected Date lastSeen;

    protected void setNode(Node<C> node) {
        this.setConnectionInfo(node.getConnectionInfo());
        this.setId(node.getId());
        this.setLastSeen(node.getLastSeen());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return getId() == node.getId() &&
                Objects.equals(getConnectionInfo(), node.getConnectionInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConnectionInfo());
    }
}
