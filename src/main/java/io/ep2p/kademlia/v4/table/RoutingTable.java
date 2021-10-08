package io.ep2p.kademlia.v4.table;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.v4.node.Node;

import java.io.Serializable;
import java.util.Vector;

public interface RoutingTable<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> extends Serializable {
    /**
     * Returns an identifier which is in a specific bucket of a routing table
     * @param id id of the routing table owner
     * @param prefix id of the bucket where we want that identifier to be
     */
    ID getIdInPrefix(ID id, int prefix);

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
     * Updates the routing table with a new value. Returns true if node didnt exist in table before
     * @param node to update
     * @return if node is added newly
     */
    boolean update(Node<ID, C> node) throws FullBucketException;
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

    Vector<B> getBuckets();

}
