package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.Node;

public interface NodeApi<C extends ConnectionInfo> {
    PingAnswer ping(Node<C> caller, Node<C> node);
    void shutdownSignal(Node<C> caller, Node<C> node);
    FindNodeAnswer<C> findNode(Node<C> caller, Node<C> node, Integer destination);
    <K, V, R> R store(Node<C> node, K key, V value);
    <K, R> R get(Node<C> node, K key);
}
