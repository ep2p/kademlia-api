package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.node.Node;

import java.io.Serializable;
import java.util.Vector;

public interface RoutingTable<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> extends Serializable {
    /**
     * @brief Returns an identifier which is in a specific bucket of a routing table
     * @param id id of the routing table owner
     * @param prefix id of the bucket where we want that identifier to be
     */
    ID getIdInPrefix(ID id, int prefix);
    /* Returns the corresponding node prefix for a given id */
    int getNodePrefix(ID id);
    /* Finds the corresponding bucket in a routing table for a given identifier */
    Bucket<ID, C> findBucket(ID id);
    /* Updates the routing table with a new value. Returns true if node didnt exist in table before */
    boolean update(Node<ID, C> node);
    void delete(Node<ID, C> node);
    /* Returns the closest nodes we know to a given id */
    FindNodeAnswer<ID, C> findClosest(ID destinationId);

    Vector<B> getBuckets();

}
