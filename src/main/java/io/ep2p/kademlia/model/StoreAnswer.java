package io.ep2p.kademlia.model;

import lombok.*;

/**
 * Model for store request reply
 * @param <ID> Number type of node ID between supported types
 * @param <K> Type of storage key
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreAnswer<ID extends Number, K> extends WatchableAnswer<ID> {
    private K key;
    private Result result;

    public enum Result {
        STORED, PASSED, FAILED, TIMEOUT
    }

    public static <ID extends Number, K> StoreAnswer<ID, K> generateWithResult(K key, StoreAnswer.Result finalResult){
        var result = new StoreAnswer<ID, K>();
        result.setResult(finalResult);
        result.setKey(key);
        return result;
    }
}
