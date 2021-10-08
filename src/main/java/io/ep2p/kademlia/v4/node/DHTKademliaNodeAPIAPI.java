package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.v4.connection.ConnectionInfo;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DHTKademliaNodeAPIAPI<ID extends Number, C extends ConnectionInfo> extends KademliaNodeAPI<ID, C> {

    <K extends Serializable, V extends Serializable> Future<StoreAnswer<ID, K>> store(K key, V value);
    <K extends Serializable, V extends Serializable> Future<GetAnswer<ID, K, V>> get(K key);
}
