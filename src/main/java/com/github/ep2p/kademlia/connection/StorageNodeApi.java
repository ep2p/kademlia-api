package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.node.Node;

public interface StorageNodeApi<ID extends Number, C extends ConnectionInfo, K, V> extends NodeApi<ID, C> {
    void onGetRequest(Node<ID, C> callerNode, Node<ID, C> requester, K key);
    void onStoreRequest(Node<ID, C> caller, Node<ID, C> requester, K key, V value);
    void onGetResult(Node<ID, C> node, K key, V value);
    void onStoreResult(Node<ID, C> node, K key, boolean successful);
}
