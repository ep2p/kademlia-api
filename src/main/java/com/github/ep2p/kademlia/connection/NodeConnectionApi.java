package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.Node;

//API for nodes to talk to each other
public interface NodeConnectionApi<ID extends Number, C extends ConnectionInfo> {
    PingAnswer<ID> ping(Node<ID, C> caller, Node<ID, C> node);
    void shutdownSignal(Node<ID, C> caller, Node<ID, C> node);
    FindNodeAnswer<ID, C> findNode(Node<ID, C> caller, Node<ID, C> node, ID destination);
    default <K, V> void storeAsync(Node<ID, C> caller, Node<ID, C> requester,  Node<ID, C> node, K key, V value){}
    default <K> void getRequest(Node<ID, C> caller, Node<ID, C> requester, Node<ID, C> node, K key){}
    default <K, V> void sendGetResults(Node<ID, C> caller, Node<ID, C> requester, K key, V value){}
    default <K> void sendStoreResults(Node<ID, C> caller, Node<ID, C> requester, K key, boolean success){}
}
