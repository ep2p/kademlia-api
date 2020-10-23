package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreAnswer<K> extends WatchableAnswer {
    private K key;
    private Result result;

    public enum Result {
        STORED, PASSED, FAILED, TIMEOUT
    }
}
