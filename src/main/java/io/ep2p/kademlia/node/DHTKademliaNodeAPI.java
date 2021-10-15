package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DHTKademliaNodeAPI<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNodeAPI<ID, C> {
    Future<StoreAnswer<ID, K>> store(K key, V value);
    Future<LookupAnswer<ID, K, V>> lookup(K key);
}
