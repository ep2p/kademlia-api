package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.PingAnswer;
import io.ep2p.kademlia.node.Node;


/**
 * API for a node to talk to other nodes
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
public interface NodeConnectionApi<ID extends Number, C extends ConnectionInfo> {
    /**
     * Ping an external node
     * @param caller Self kademlia node
     * @param node External node
     * @return PingAnswer
     */
    PingAnswer<ID> ping(Node<ID, C> caller, Node<ID, C> node);
    /**
     * Ping an external node
     * @param caller Self kademlia node
     * @param node External node
     */
    void shutdownSignal(Node<ID, C> caller, Node<ID, C> node);

    /**
     * Find nodes closed destination using an external node
     * @param caller Self kademlia node
     * @param node External node
     * @param destination destination lookup
     * @return FindNodeAnswer
     */
    FindNodeAnswer<ID, C> findNode(Node<ID, C> caller, Node<ID, C> node, ID destination);

    /**
     * Store some data on nodes async
     * @param caller Self kademlia node
     * @param requester Origin node that requested to store data
     * @param node External node to forward the request to
     * @param key Store data key
     * @param value Store data value
     * @param <K> Type of key
     * @param <V> Type of value
     */
    default <K, V> void storeAsync(Node<ID, C> caller, Node<ID, C> requester,  Node<ID, C> node, K key, V value){}
    /**
     * Look up for some key's value
     * @param caller Self kademlia node
     * @param requester Origin node that requested to store data
     * @param node External node to forward the request to
     * @param key Get data request key
     * @param <K> Type of key
     */
    default <K> void getRequest(Node<ID, C> caller, Node<ID, C> requester, Node<ID, C> node, K key){}
    /**
     * Sends back a get request result to its origin requester
     * @param caller Self kademlia node
     * @param requester Origin node that requested to store data
     * @param key Get data request key
     * @param <K> Type of key
     */
    default <K, V> void sendGetResults(Node<ID, C> caller, Node<ID, C> requester, K key, V value){}
    /**
     * Sends back a store request result to its origin requester
     * @param caller Self kademlia node
     * @param requester Origin node that requested to store data
     * @param key Get data request key
     * @param <K> Type of key
     */
    default <K> void sendStoreResults(Node<ID, C> caller, Node<ID, C> requester, K key, boolean success){}
}
