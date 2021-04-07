package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.node.Node;

/**
 * @brief Node api for storage based kademlia node (kademlia repository node)
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 * @param <K> Type of storage key
 * @param <V> Type of storage value
 */
public interface StorageNodeApi<ID extends Number, C extends ConnectionInfo, K, V> extends NodeApi<ID, C> {
    /**
     * @brief Called when a get request reaches node
     * @param caller node that forwarded or is sending the request
     * @param requester origin node that asked for data
     * @param key key of data
     */
    void onGetRequest(Node<ID, C> caller, Node<ID, C> requester, K key);
    /**
     * @brief Called when a store request reaches node
     * @param caller node that forwarded or is sending the request
     * @param requester origin node that asked for data
     * @param key key of data
     * @param value value of data to store
     */
    void onStoreRequest(Node<ID, C> caller, Node<ID, C> requester, K key, V value);
    /**
     * @brief Called when a store result reaches node
     * @param node node that stored the data
     * @param key key of data
     * @param successful determines if data is successfully stored
     */
    void onStoreResult(Node<ID, C> node, K key, boolean successful);
    /**
     * @brief Called when a get request result reaches node
     * @param node node that stored / found the data
     * @param key key of data
     * @param value value of the key
     */
    void onGetResult(Node<ID, C> node, K key, V value);
}
