package io.ep2p.kademlia.node;

import java.util.List;

/**
 * @brief Kademlia storage repository
 * @param <K> storage key type
 * @param <V> storage value type
 */
public interface KademliaRepository<K, V> {
    void store(K key, V value);
    V get(K key);
    void remove(K key);
    boolean contains(K key);
    List<K> getKeys();
}
