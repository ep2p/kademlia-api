package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAnswer<K, V> extends WatchableAnswer {
    private K key;
    private V value;
    private Result result;

    public enum Result {
        PASSED, FOUND, FAILED, TIMEOUT
    }
}
