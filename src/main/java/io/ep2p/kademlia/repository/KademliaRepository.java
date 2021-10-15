package io.ep2p.kademlia.repository;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.DHTKey;
import io.ep2p.kademlia.node.Node;

import java.io.Serializable;

public interface KademliaRepository<ID extends Number, C extends ConnectionInfo, K extends DHTKey<ID>, V extends Serializable> {
    void store(Node<ID, C> node, K key, V value);
    V get(Node<ID, C> node, K key);
    void remove(Node<ID, C> node, K key);
    boolean contains(Node<ID, C> node, K key);
}
