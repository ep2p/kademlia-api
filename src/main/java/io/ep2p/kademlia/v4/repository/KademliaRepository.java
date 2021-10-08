package io.ep2p.kademlia.v4.repository;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.node.Node;

import java.io.Serializable;

public interface KademliaRepository<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> {
    void store(Node<ID, C> node, K key, V value);
    V get(Node<ID, C> node, K key);
    void remove(Node<ID, C> node, K key);
    boolean contains(Node<ID, C> node, K key);
}
