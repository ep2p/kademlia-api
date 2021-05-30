package io.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Model for store request reply
 * @param <ID> Number type of node ID between supported types
 * @param <K> Type of storage key
 */
@Getter
@Setter
public class StoreAnswer<ID extends Number, K> extends WatchableAnswer<ID> {
    private K key;
    private Result result;

    public enum Result {
        STORED, PASSED, FAILED, TIMEOUT
    }
}
