package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.node.Node;

public interface NodeApi<C extends ConnectionInfo> {
    void ping(Node<C> node, ResponseListener<Void> responseListener);
    <R> void findNode(Node<C> node, ResponseListener<R> rResponseListener);
    <K, V, R> void store(Node<C> node, K key, V value, ResponseListener<R> responseListener);
    <K, R> void get(Node<C> node, K key, ResponseListener<R> responseListener);
}
