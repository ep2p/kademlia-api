package io.ep2p.kademlia.node.decorators;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.NodeDecorator;

import java.util.Date;

/**
 * Node decorator to hold last seen of a node
 * @param <ID> Node ID type
 * @param <C> Node ConnectionInfo type
 */
public class DateAwareNodeDecorator<ID extends Number, C extends ConnectionInfo> extends NodeDecorator<ID, C> {
    private Date lastSeen = new Date();

    public DateAwareNodeDecorator(Node<ID, C> node) {
        super(node);
    }

    @Override
    public void setLastSeen(Date date) {
        this.lastSeen = date;
    }

    @Override
    public Date getLastSeen() {
        return this.lastSeen;
    }
}
