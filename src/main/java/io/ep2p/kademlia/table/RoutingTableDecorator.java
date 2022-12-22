package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.util.List;

public abstract class RoutingTableDecorator<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> implements RoutingTable<ID, C, B> {

    protected final RoutingTable<ID, C, B> routingTable;

    protected RoutingTableDecorator(RoutingTable<ID, C, B> routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public int getNodePrefix(ID id) {
        return this.routingTable.getNodePrefix(id);
    }

    @Override
    public Bucket<ID, C> findBucket(ID id) {
        return this.routingTable.findBucket(id);
    }

    @Override
    public boolean update(Node<ID, C> node) throws FullBucketException {
        return this.routingTable.update(node);
    }

    @Override
    public void forceUpdate(Node<ID, C> node) {
        this.routingTable.forceUpdate(node);
    }

    @Override
    public void delete(Node<ID, C> node) {
        this.routingTable.delete(node);
    }

    @Override
    public FindNodeAnswer<ID, C> findClosest(ID destinationId) {
        return this.routingTable.findClosest(destinationId);
    }

    @Override
    public boolean contains(ID nodeId) {
        return this.routingTable.contains(nodeId);
    }

    @Override
    public List<B> getBuckets() {
        return this.routingTable.getBuckets();
    }

    @Override
    public ID getDistance(ID id) {
        return this.routingTable.getDistance(id);
    }

    @Override
    public ExternalNode<ID, C> getExternalNode(Node<ID, C> node) {
        return this.routingTable.getExternalNode(node);
    }
}
