package io.ep2p.kademlia.model;

import lombok.*;

import java.io.Serializable;

/**
 * Model for lookup request response possibly containing value of requested key
 * @param <ID> Number type of node ID between supported types
 * @param <K> Type of storage key
 * @param <V> Type of storage value
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LookupAnswer<ID extends Number, K, V> extends WatchableAnswer<ID> {
    private K key;
    private V value;
    private Result result = Result.FAILED;

    public enum Result {
        PASSED, FOUND, FAILED, TIMEOUT
    }

    public static <ID extends Number, K extends Serializable, V extends Serializable> LookupAnswer<ID, K, V> generateWithResult(K key, LookupAnswer.Result finalResult){
        LookupAnswer<ID, K, V> result = new LookupAnswer<ID, K, V>();
        result.setResult(finalResult);
        result.setKey(key);
        return result;
    }
}
