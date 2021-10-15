package io.ep2p.kademlia.helpers;

import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.repository.KademliaRepository;

import java.util.HashMap;
import java.util.Map;

public class SampleRepository implements KademliaRepository<Integer, String> {
    protected final Map<Integer, String> data = new HashMap<>();

    @Override
    public void store(Integer key, String value) {
        data.putIfAbsent(key, value);
    }

    @Override
    public String get(Integer key) {
        return data.get(key);
    }

    @Override
    public void remove(Integer key) {
        data.remove(key);
    }

    @Override
    public boolean contains(Integer key) {
        return data.containsKey(key);
    }
}
