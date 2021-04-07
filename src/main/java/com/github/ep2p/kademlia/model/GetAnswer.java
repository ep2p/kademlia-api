package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Model for get request response possibly containing value of requested key
 * @param <ID> Number type of node ID between supported types
 * @param <K> Type of storage key
 * @param <V> Type of storage value
 */
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
