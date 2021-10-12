package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.StoreAnswer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public interface DHTKademliaNodeAPI<ID extends Number, C extends ConnectionInfo> extends KademliaNodeAPI<ID, C> {

    <K extends Serializable, V extends Serializable> StoreAnswer<ID, K> store(K key, V value, int timeoutVal, TimeUnit timeoutUnit);
    <K extends Serializable, V extends Serializable> StoreAnswer<ID, K> get(K key, int timeoutVal, TimeUnit timeoutUnit);
}
