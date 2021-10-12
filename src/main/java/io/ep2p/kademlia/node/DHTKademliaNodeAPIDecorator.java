package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;

import java.io.Serializable;
import java.util.concurrent.Future;

public abstract class DHTKademliaNodeAPIDecorator<ID extends Number, C extends ConnectionInfo> extends KademliaNodeAPIDecorator<ID, C> implements DHTKademliaNodeAPI<ID, C> {

    protected DHTKademliaNodeAPIDecorator(DHTKademliaNodeAPI<ID, C> kademliaNode) {
        super(kademliaNode);
    }

    @Override
    public <K extends Serializable, V extends Serializable> Future<StoreAnswer<ID, K>> store(K key, V value) {
        return ((DHTKademliaNodeAPI<ID, C>) getKademliaNode()).store(key, value);
    }

    @Override
    public <K extends Serializable, V extends Serializable> Future<GetAnswer<ID, K, V>> get(K key) {
        return ((DHTKademliaNodeAPI<ID, C>) getKademliaNode()).get(key);
    }
}
