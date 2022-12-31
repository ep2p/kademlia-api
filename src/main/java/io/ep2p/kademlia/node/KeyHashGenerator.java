package io.ep2p.kademlia.node;

import java.io.Serializable;

/**
 * @param <I> Type of the node ID
 * @param <K> Serializable type of the Key to hash
 */
public interface KeyHashGenerator<I extends Number, K extends Serializable> {
    /**
     * @param key serializable key to hash
     * @return hashed key with type of ID
     */
    I generateHash(K key);
}
