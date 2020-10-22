package com.github.ep2p.kademlia.node;

import java.util.List;

public interface KademliaRepository<K, V> {
    void store(K key, V value);
    V get(K key);
    void remove(K key);
    boolean contains(K key);
    List<K> getKeys();
}
