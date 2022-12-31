package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.io.Serializable;
import java.util.List;

public interface Bucket<I extends Number, C extends ConnectionInfo> extends Serializable {
    int getId();
    int size();
    boolean contains(I id);
    boolean contains(Node<I, C> node);
    /**
     * Add a node to the front of the bucket
     * @param node to add to this bucket
     */
    void add(ExternalNode<I, C> node);
    void remove(Node<I, C> node);
    void remove(I nodeId);
    /**
     * Push a node to the front of a bucket. Called when a node is already in bucket and brings them to front of the bucket as they are a living node
     * @param node the node to push
     */
    void pushToFront(ExternalNode<I, C> node);
    ExternalNode<I, C> getNode(I id);
    List<I> getNodeIds();
}
