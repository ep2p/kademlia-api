package io.ep2p.kademlia.helpers;

import io.ep2p.kademlia.repository.KademliaRepository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SampleRepository<K extends Serializable> implements KademliaRepository<K, String> {
    protected final Map<K, String> data = new HashMap<>();

    @Override
    public void store(K key, String value) {
        data.putIfAbsent(key, value);
    }

    @Override
    public String get(K key) {
        return data.get(key);
    }

    @Override
    public void remove(K key) {
        data.remove(key);
    }

    @Override
    public boolean contains(K key) {
        return data.containsKey(key);
    }
}
