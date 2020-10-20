package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Node<C extends ConnectionInfo> implements Serializable {
    protected int id;
    protected ConnectionInfo connection;
    protected Date lastSeen;

    protected void setNode(Node<C> node) {
        this.setConnection(node.getConnection());
        this.setId(node.getId());
        this.setLastSeen(node.getLastSeen());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return getId() == node.getId() &&
                Objects.equals(getConnection(), node.getConnection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConnection());
    }
}
