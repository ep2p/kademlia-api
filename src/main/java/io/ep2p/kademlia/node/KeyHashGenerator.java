package io.ep2p.kademlia.node;

import java.io.Serializable;

/**
 * @param <ID> Type of the node ID
 * @param <K> Serializable type of the Key to hash
 */
public interface KeyHashGenerator<ID extends Number, K extends Serializable> {
    /**
     * @param key serializable key to hash
     * @return hashed key with type of ID
     */
    ID generateHash(K key);
}
