package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAnswer<K, V> extends Answer {
    private K key;
    private V value;
    private Action action;

    public enum Action {
        PASSED, FOUND, FAILED
    }
}
