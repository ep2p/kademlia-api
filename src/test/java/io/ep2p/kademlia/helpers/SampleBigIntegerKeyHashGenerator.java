package io.ep2p.kademlia.helpers;

import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.util.BoundedHashUtil;
import lombok.SneakyThrows;

import java.math.BigInteger;

public class SampleBigIntegerKeyHashGenerator implements KeyHashGenerator<BigInteger, BigInteger> {
    private final int size;

    public SampleBigIntegerKeyHashGenerator(int size) {
        this.size = size;
    }

    @SneakyThrows
    @Override
    public BigInteger generateHash(BigInteger key) {
        return new BoundedHashUtil(size).hash(key, BigInteger.class);
    }
}
