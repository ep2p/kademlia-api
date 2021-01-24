package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

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
