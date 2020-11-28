package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

import java.io.Serializable;
import java.util.List;

public interface Bucket<ID extends Number, C extends ConnectionInfo> extends Serializable {
    int getId();
    int size();
    boolean contains(ID id);
    boolean contains(Node<ID, C> node);
    /* Add a node to the front of the bucket */
    void add(Node<ID, C> node);
    void remove(Node<ID, C> node);
    void remove(ID nodeId);
    /* Push a node to the front of a bucket */
    /* Called when a node is already in bucket and brings them to front of the bucket as they are a living node */
    void pushToFront(ID id);
    Node<ID, C> getNode(ID id);
    List<ID> getNodeIds();
}
