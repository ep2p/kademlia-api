package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.io.Serializable;
import java.util.List;

public interface RoutingTable<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> extends Serializable {

    /**
     * Returns the corresponding node prefix for a given id
     * @param id node to look for prefix
     * @return prefix
     */
    int getNodePrefix(ID id);

    /**
     * Finds the corresponding bucket in a routing table for a given identifier
     * @param id node to find bucket for
     * @return bucket
     */
    Bucket<ID, C> findBucket(ID id);

    /**
     * Updates the routing table with a new node. Returns true if node didnt exist in table before
     * @param node to update
     * @return if node is added newly
     */
    boolean update(Node<ID, C> node) throws FullBucketException;

    /**
     * Updating the routing table with a new node.
     * Should definitely add the new node to the routing table even if bucket is full
     * @param node to update
     */
    void forceUpdate(Node<ID, C> node);

    /**
     * Delete node from table
     * @param node to delete
     */
    void delete(Node<ID, C> node);

    /**
     * Returns the closest nodes we know to a given id
     * @param destinationId lookup
     * @return result for closest nodes to destination
     */
    FindNodeAnswer<ID, C> findClosest(ID destinationId);

    boolean contains(ID nodeId);

    List<B> getBuckets();

    ID getDistance(ID id);

    ExternalNode<ID,C> getExternalNode(Node<ID,C> node);
}
