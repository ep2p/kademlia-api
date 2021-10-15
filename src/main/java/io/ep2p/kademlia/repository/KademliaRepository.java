package io.ep2p.kademlia.repository;

import java.io.Serializable;

public interface KademliaRepository<K extends Serializable, V extends Serializable> {
    void store(K key, V value);
    V get(K key);
    void remove(K key);
    boolean contains(K key);
}
