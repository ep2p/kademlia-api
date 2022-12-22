package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.repository.KademliaRepository;

import java.io.Serializable;
import java.util.concurrent.Future;

public abstract class DHTKademliaNodeAPIDecorator<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNodeAPIDecorator<ID, C> implements DHTKademliaNodeAPI<ID, C, K, V> {

    protected DHTKademliaNodeAPIDecorator(DHTKademliaNodeAPI<ID, C, K, V> kademliaNode) {
        super(kademliaNode);
    }

    @Override
    public Future<StoreAnswer<ID, C, K>> store(K key, V value) throws DuplicateStoreRequest {
        return ((DHTKademliaNodeAPI<ID, C, K, V>) getKademliaNode()).store(key, value);
    }

    @Override
    public Future<LookupAnswer<ID, C, K, V>> lookup(K key) {
        return ((DHTKademliaNodeAPI<ID, C, K, V>) getKademliaNode()).lookup(key);
    }

    @Override
    public KademliaRepository<K, V> getKademliaRepository() {
        return ((DHTKademliaNodeAPI<ID, C, K, V>) getKademliaNode()).getKademliaRepository();
    }

    @Override
    public KeyHashGenerator<ID, K> getKeyHashGenerator() {
        return ((DHTKademliaNodeAPI<ID, C, K, V>) getKademliaNode()).getKeyHashGenerator();
    }
}
