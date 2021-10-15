package io.ep2p.kademlia.model;

import java.io.Serializable;

public interface DHTKey<ID extends Number> extends Serializable {
    ID getHash();
}
