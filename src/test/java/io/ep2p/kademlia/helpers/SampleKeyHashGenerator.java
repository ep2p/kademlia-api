package io.ep2p.kademlia.helpers;

import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.util.BoundedHashUtil;
import lombok.SneakyThrows;

public class SampleKeyHashGenerator implements KeyHashGenerator<Integer, Integer> {
    private final int size;

    public SampleKeyHashGenerator(int size) {
        this.size = size;
    }

    @SneakyThrows
    @Override
    public Integer generateHash(Integer key) {
        return new BoundedHashUtil(size).hash(key, Integer.class);
    }
}
