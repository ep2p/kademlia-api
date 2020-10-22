package com.github.ep2p.kademlia.node;

public interface KademliaRepository<K, V> {
    void store(K key, V value);
    V get(K key);
    void remove(K key);
}
