package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.node.Node;

public interface StorageP2PApi<C extends ConnectionInfo, K, V> extends P2PApi<C> {
    void onGetRequest(Node<C> callerNode, Node<C> requester, K key);
    void onStoreRequest(Node<C> caller, Node<C> requester, K key, V value);
    void onGetResult(Node<C> node, K key, V value);
    void onStoredResult(Node<C> node, K key);
}
