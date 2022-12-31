package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.repository.KademliaRepository;

import java.io.Serializable;
import java.util.concurrent.Future;

public abstract class DHTKademliaNodeAPIDecorator<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNodeAPIDecorator<I, C> implements DHTKademliaNodeAPI<I, C, K, V> {

    protected DHTKademliaNodeAPIDecorator(DHTKademliaNodeAPI<I, C, K, V> kademliaNode) {
        super(kademliaNode);
    }

    @Override
    public Future<StoreAnswer<I, C, K>> store(K key, V value) {
        return ((DHTKademliaNodeAPI<I, C, K, V>) getKademliaNode()).store(key, value);
    }

    @Override
    public Future<LookupAnswer<I, C, K, V>> lookup(K key) {
        return ((DHTKademliaNodeAPI<I, C, K, V>) getKademliaNode()).lookup(key);
    }

    @Override
    public KademliaRepository<K, V> getKademliaRepository() {
        return ((DHTKademliaNodeAPI<I, C, K, V>) getKademliaNode()).getKademliaRepository();
    }

    @Override
    public KeyHashGenerator<I, K> getKeyHashGenerator() {
        return ((DHTKademliaNodeAPI<I, C, K, V>) getKademliaNode()).getKeyHashGenerator();
    }
}
