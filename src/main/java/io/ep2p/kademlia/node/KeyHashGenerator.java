package io.ep2p.kademlia.node;

import java.io.Serializable;

public interface KeyHashGenerator<ID, K extends Serializable> {
    ID generateHash(K key);
}
