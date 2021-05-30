package io.ep2p.kademlia.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleRepository implements KademliaRepository<Integer, String> {
    private final Map<Integer, String> data = new HashMap<>();

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

    @Override
    public List<Integer> getKeys() {
        return new ArrayList<>(data.keySet());
    }
}
