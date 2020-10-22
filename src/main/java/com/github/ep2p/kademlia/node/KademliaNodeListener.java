package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;

import java.util.List;

public interface KademliaNodeListener<C extends ConnectionInfo> {
    default void onReferencedNodesUpdate(KademliaNode<C> kademliaNode, List<Node<C>> referencedNodes){}
    default void onBootstrapDone(KademliaNode<C> kademliaNode){}
    default void onShutdownComplete(KademliaNode<C> kademliaNode){}
    default void onStartupComplete(KademliaNode<C> kademliaNode){}
    default <K, V> void onKeyLookupResult(Node<C> node, K key, V value){}
    default <K> void onKeyStoredResult(Node<C> node, K key, boolean success){}
    class Default<C extends ConnectionInfo> implements KademliaNodeListener<C> {}
}
