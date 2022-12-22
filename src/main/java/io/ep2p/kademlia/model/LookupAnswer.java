package io.ep2p.kademlia.model;

import io.ep2p.kademlia.connection.ConnectionInfo;
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
public class LookupAnswer<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends Answer<ID, C> {
    private K key;
    private V value;
    private Result result = Result.FAILED;

    public enum Result {
        PASSED, FOUND, FAILED
    }

}
