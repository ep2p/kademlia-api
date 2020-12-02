package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class GetAnswer<ID extends Number, K, V> extends WatchableAnswer<ID> {
    private K key;
    private V value;
    private Result result;

    public enum Result {
        PASSED, FOUND, FAILED, TIMEOUT
    }
}
