package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.repository.KademliaRepository;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * KademliaNodeAPI for DHT operations, extends KademliaNodeAPI
 * @param <I> Type of the node ID
 * @param <C> Type of the node ConnectionInfo
 * @param <K> Type of the serializable Key
 * @param <V> Type of the serializable Value
 */
public interface DHTKademliaNodeAPI<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends KademliaNodeAPI<I, C> {
    /**
     * @param key Serializable key of the data to store
     * @param value Serializable value of the data to store
     * @return Future object of StoreAnswer, contains result status and node that stored the data
     */
    Future<StoreAnswer<I, C, K>> store(K key, V value);
    /**
     * @param key Serializable key of the data to look up
     * @return Future object of LookupAnswer, contains value, result status and node that stored the data
     */
    Future<LookupAnswer<I, C, K, V>> lookup(K key);
    /**
     * @return KademliaRepository of this node
     */
    KademliaRepository<K, V> getKademliaRepository();
    /**
     * @return KeyHashGenerator of this node
     */
    KeyHashGenerator<I, K> getKeyHashGenerator();
}
