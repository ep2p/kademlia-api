package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.util.List;

public abstract class RoutingTableDecorator<I extends Number, C extends ConnectionInfo, B extends Bucket<I, C>> implements RoutingTable<I, C, B> {

    protected final RoutingTable<I, C, B> routingTable;

    protected RoutingTableDecorator(RoutingTable<I, C, B> routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public int getNodePrefix(I id) {
        return this.routingTable.getNodePrefix(id);
    }

    @Override
    public Bucket<I, C> findBucket(I id) {
        return this.routingTable.findBucket(id);
    }

    @Override
    public boolean update(Node<I, C> node) throws FullBucketException {
        return this.routingTable.update(node);
    }

    @Override
    public void forceUpdate(Node<I, C> node) {
        this.routingTable.forceUpdate(node);
    }

    @Override
    public void delete(Node<I, C> node) {
        this.routingTable.delete(node);
    }

    @Override
    public FindNodeAnswer<I, C> findClosest(I destinationId) {
        return this.routingTable.findClosest(destinationId);
    }

    @Override
    public boolean contains(I nodeId) {
        return this.routingTable.contains(nodeId);
    }

    @Override
    public List<B> getBuckets() {
        return this.routingTable.getBuckets();
    }

    @Override
    public I getDistance(I id) {
        return this.routingTable.getDistance(id);
    }

    @Override
    public ExternalNode<I, C> getExternalNode(Node<I, C> node) {
        return this.routingTable.getExternalNode(node);
    }
}
