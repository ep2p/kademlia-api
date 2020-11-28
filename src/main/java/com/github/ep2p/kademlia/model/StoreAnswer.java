package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreAnswer<ID extends Number, K> extends WatchableAnswer<ID> {
    private K key;
    private Result result;

    public enum Result {
        STORED, PASSED, FAILED, TIMEOUT
    }
}
