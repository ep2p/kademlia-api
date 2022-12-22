package io.ep2p.kademlia.model;

import io.ep2p.kademlia.connection.ConnectionInfo;
import lombok.*;

import java.io.Serializable;

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
@ToString
public class StoreAnswer<ID extends Number, C extends ConnectionInfo, K extends Serializable> extends Answer<ID, C> {
    private K key;
    private Result result = Result.FAILED;

    public enum Result {
        STORED, PASSED, FAILED
    }
}
