package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.exception.NodeIsOfflineException;

import java.util.List;

public interface KademliaNodeListener<ID extends Number, C extends ConnectionInfo, K, V> {
    default void onReferencedNodesUpdate(KademliaNode<ID, C> kademliaNode, List<Node<ID, C>> referencedNodes){}
    default void onBootstrapDone(KademliaNode<ID, C> kademliaNode){}
    default void onNewNodeAvailable(KademliaNode<ID, C> kademliaNode, Node<ID, C> node){}
    default void onShutdownComplete(KademliaNode<ID, C> kademliaNode){}
    default void onBeforeShutdown(KademliaNode<ID, C> kademliaNode){}
    default void onStartupComplete(KademliaNode<ID, C> kademliaNode){}
    default void onPing(KademliaNode<ID, C> kademliaNode, Node<ID, C> node) throws NodeIsOfflineException {}
    default void onKeyLookupResult(KademliaNode<ID, C> kademliaNode, Node<ID, C> node, K key, V value){}
    default void onKeyStoredResult(KademliaNode<ID, C> kademliaNode, Node<ID, C> node, K key, boolean success){}
    class Default<ID extends Number, C extends ConnectionInfo> implements KademliaNodeListener<ID, C, Void, Void> {}
}
