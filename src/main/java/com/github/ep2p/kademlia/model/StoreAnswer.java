package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreAnswer<K> extends Answer {
    private K key;
    private Action action;

    public enum Action {
        STORED, PASSED, FAILED
    }
}
