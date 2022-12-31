package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.io.Serializable;
import java.util.List;

public interface RoutingTable<I extends Number, C extends ConnectionInfo, B extends Bucket<I, C>> extends Serializable {
    /**
     * Returns the corresponding node prefix for a given id
     * @param id node to look for prefix
     * @return prefix
     */
    int getNodePrefix(I id);

    /**
     * Finds the corresponding bucket in a routing table for a given identifier
     * @param id node to find bucket for
     * @return bucket
     */
    Bucket<I, C> findBucket(I id);

    /**
     * Updates the routing table with a new node. Returns true if node didnt exist in table before
     * @param node to update
     * @return if node is added newly
     * @throws FullBucketException if bucket that should hold the node is full
     */
    boolean update(Node<I, C> node) throws FullBucketException;

    /**
     * Updating the routing table with a new node.
     * Should definitely add the new node to the routing table even if bucket is full
     * @param node to update
     */
    void forceUpdate(Node<I, C> node);

    /**
     * Delete node from table
     * @param node to delete
     */
    void delete(Node<I, C> node);

    /**
     * Returns the closest nodes we know to a given id
     * @param destinationId lookup
     * @return result for closest nodes to destination
     */
    FindNodeAnswer<I, C> findClosest(I destinationId);

    boolean contains(I nodeId);

    List<B> getBuckets();

    I getDistance(I id);

    ExternalNode<I,C> getExternalNode(Node<I,C> node);
}
