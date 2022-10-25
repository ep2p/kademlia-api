package io.ep2p.kademlia.helpers;

import io.ep2p.kademlia.repository.KademliaRepository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SampleBigIntegerRepository implements KademliaRepository<BigInteger, String> {
    protected final Map<BigInteger, String> data = new HashMap<>();

    @Override
    public void store(BigInteger key, String value) {
        data.putIfAbsent(key, value);
    }

    @Override
    public String get(BigInteger key) {
        return data.get(key);
    }

    @Override
    public void remove(BigInteger key) {
        data.remove(key);
    }

    @Override
    public boolean contains(BigInteger key) {
        return data.containsKey(key);
    }
}
