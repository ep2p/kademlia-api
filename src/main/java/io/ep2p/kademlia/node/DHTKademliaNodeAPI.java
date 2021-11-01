package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.repository.KademliaRepository;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * KademliaNodeAPI for DHT operations, extends KademliaNodeAPI<ID, C>
 * @param <ID> Type of the node ID
 * @param <C> Type of the node ConnectionInfo
 * @param <K> Type of the serializable Key
 * @param <V> Type of the serializable Value
 */
public interface DHTKademliaNodeAPI<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNodeAPI<ID, C> {
    /**
     * @param key Serializable key of the data to store
     * @param value Serializable value of the data to store
     * @return Future object of StoreAnswer, contains result status and node that stored the data
     */
    Future<StoreAnswer<ID, K>> store(K key, V value) throws DuplicateStoreRequest;
    /**
     * @param key Serializable key of the data to look up
     * @return Future object of LookupAnswer, contains value, result status and node that stored the data
     */
    Future<LookupAnswer<ID, K, V>> lookup(K key);
    /**
     * @return KademliaRepository of this node
     */
    KademliaRepository<K, V> getKademliaRepository();
    /**
     * @return KeyHashGenerator of this node
     */
    KeyHashGenerator<ID, K> getKeyHashGenerator();
}
