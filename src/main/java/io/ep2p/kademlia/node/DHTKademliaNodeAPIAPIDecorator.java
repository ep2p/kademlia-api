package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;

import java.io.Serializable;
import java.util.concurrent.Future;

public abstract class DHTKademliaNodeAPIAPIDecorator<ID extends Number, C extends ConnectionInfo> extends KademliaNodeAPIDecorator<ID, C> implements DHTKademliaNodeAPIAPI<ID, C> {

    protected DHTKademliaNodeAPIAPIDecorator(DHTKademliaNodeAPIAPI<ID, C> kademliaNode) {
        super(kademliaNode);
    }

    @Override
    public <K extends Serializable, V extends Serializable> Future<StoreAnswer<ID, K>> store(K key, V value) {
        return ((DHTKademliaNodeAPIAPI<ID, C>) getKademliaNode()).store(key, value);
    }

    @Override
    public <K extends Serializable, V extends Serializable> Future<GetAnswer<ID, K, V>> get(K key) {
        return ((DHTKademliaNodeAPIAPI<ID, C>) getKademliaNode()).get(key);
    }
}
