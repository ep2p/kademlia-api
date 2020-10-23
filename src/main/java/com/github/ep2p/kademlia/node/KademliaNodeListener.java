package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.exception.NodeIsOfflineException;

import java.util.List;

public interface KademliaNodeListener<C extends ConnectionInfo, K, V> {
    default void onReferencedNodesUpdate(KademliaNode<C> kademliaNode, List<Node<C>> referencedNodes){}
    default void onBootstrapDone(KademliaNode<C> kademliaNode){}
    default void onNewNodeAvailable(KademliaNode<C> kademliaNode, Node<C> node){}
    default void onShutdownComplete(KademliaNode<C> kademliaNode){}
    default void onBeforeShutdown(KademliaNode<C> kademliaNode){}
    default void onStartupComplete(KademliaNode<C> kademliaNode){}
    default void onPing(KademliaNode<C> kademliaNode, Node<C> node) throws NodeIsOfflineException {}
    default void onKeyLookupResult(KademliaNode<C> kademliaNode, Node<C> node, K key, V value){}
    default void onKeyStoredResult(KademliaNode<C> kademliaNode, Node<C> node, K key, boolean success){}
    class Default<C extends ConnectionInfo> implements KademliaNodeListener<C, Void, Void> {}
}
