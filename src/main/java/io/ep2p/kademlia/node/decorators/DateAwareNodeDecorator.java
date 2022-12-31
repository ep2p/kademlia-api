package io.ep2p.kademlia.node.decorators;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.NodeDecorator;
import lombok.ToString;

import java.util.Date;

/**
 * Node decorator to hold last seen of a node
 * @param <I> Node ID type
 * @param <C> Node ConnectionInfo type
 */
@ToString(callSuper = true)
public class DateAwareNodeDecorator<I extends Number, C extends ConnectionInfo> extends NodeDecorator<I, C> {
    private Date lastSeen = new Date();

    public DateAwareNodeDecorator(Node<I, C> node) {
        super(node);
    }

    public void setLastSeen(Date date) {
        this.lastSeen = date;
    }

    public Date getLastSeen() {
        return this.lastSeen;
    }
}
