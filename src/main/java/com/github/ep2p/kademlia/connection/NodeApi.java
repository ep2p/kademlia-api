package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.Node;

//API for nodes to talk to each other
public interface NodeApi<C extends ConnectionInfo> {
    PingAnswer ping(Node<C> caller, Node<C> node);
    void shutdownSignal(Node<C> caller, Node<C> node);
    FindNodeAnswer<C> findNode(Node<C> caller, Node<C> node, Integer destination);
    <K, V> void storeAsync(Node<C> caller, Node<C> requester,  Node<C> node, K key, V value);
    <K> void getRequest(Node<C> caller, Node<C> requester, Node<C> node, K key);
    <K, V> void sendGetResults(Node<C> caller, Node<C> requester, K key, V value);
    <K> void sendStoreResults(Node<C> caller, Node<C> requester, K key, boolean success);
}
