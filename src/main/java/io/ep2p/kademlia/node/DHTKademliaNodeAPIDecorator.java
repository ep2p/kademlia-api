package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.StoreAnswer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public abstract class DHTKademliaNodeAPIDecorator<ID extends Number, C extends ConnectionInfo> extends KademliaNodeAPIDecorator<ID, C> implements DHTKademliaNodeAPI<ID, C> {

    protected DHTKademliaNodeAPIDecorator(DHTKademliaNodeAPI<ID, C> kademliaNode) {
        super(kademliaNode);
    }

    @Override
    public <K extends Serializable, V extends Serializable> StoreAnswer<ID, K> store(K key, V value, int timeoutVal, TimeUnit timeoutUnit) {
        return ((DHTKademliaNodeAPI<ID, C>) getKademliaNode()).store(key, value, timeoutVal, timeoutUnit);
    }

    @Override
    public <K extends Serializable, V extends Serializable> StoreAnswer<ID, K> get(K key, int timeoutVal, TimeUnit timeoutUnit) {
        return ((DHTKademliaNodeAPI<ID, C>) getKademliaNode()).get(key, timeoutVal, timeoutUnit);
    }
}
