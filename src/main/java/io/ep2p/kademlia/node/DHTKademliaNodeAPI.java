package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DHTKademliaNodeAPI<ID extends Number, C extends ConnectionInfo> extends KademliaNodeAPI<ID, C> {

    <K extends Serializable, V extends Serializable> Future<StoreAnswer<ID, K>> store(K key, V value);
    <K extends Serializable, V extends Serializable> Future<GetAnswer<ID, K, V>> get(K key);
}
