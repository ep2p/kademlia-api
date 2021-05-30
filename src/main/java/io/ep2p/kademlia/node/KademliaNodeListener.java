package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.NodeIsOfflineException;

import java.util.List;

/**
 * Listener to register on KademliaNode class to be called on certain events
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 * @param <K> storage key type
 * @param <V> storage value type
 */
public interface KademliaNodeListener<ID extends Number, C extends ConnectionInfo, K, V> {
    /**
     * Called when referenced nodes (chosen nodes to keep connection to and check for aliveness periodically) are updated
     * @param kademliaNode reference to the kademlia node that is calling the listener
     * @param referencedNodes referenced nodes
     */
    default void onReferencedNodesUpdate(KademliaNode<ID, C> kademliaNode, List<Node<ID, C>> referencedNodes){}
    /**
     * Called when bootstrapping is done
     * @param kademliaNode reference to the kademlia node that is calling the listener
     */
    default void onBootstrapDone(KademliaNode<ID, C> kademliaNode){}
    /**
     * Called when a new alive node is discovered and added to routing table
     * @param kademliaNode reference to the kademlia node that is calling the listener
     * @param node new node that is available
     */
    default void onNewNodeAvailable(KademliaNode<ID, C> kademliaNode, Node<ID, C> node){}
    /**
     * Called when node shutdown is completed
     * @param kademliaNode reference to the kademlia node that is calling the listener
     */
    default void onShutdownComplete(KademliaNode<ID, C> kademliaNode){}
    /**
     * Called before shutting down the node
     * @param kademliaNode reference to the kademlia node that is calling the listener
     */
    default void onBeforeShutdown(KademliaNode<ID, C> kademliaNode){}
    /**
     * Called when node startup is completed
     * @param kademliaNode reference to the kademlia node that is calling the listener
     */
    default void onStartupComplete(KademliaNode<ID, C> kademliaNode){}
    /**
     * Called when node is pinged (after ping is processed)
     * @param kademliaNode reference to the kademlia node that is calling the listener
     */
    default void onPing(KademliaNode<ID, C> kademliaNode, Node<ID, C> node) throws NodeIsOfflineException {}

    /**
     * Called when get request result is back to the node {@link KademliaRepositoryNode}
     * @param kademliaNode reference to the kademlia node that is calling the listener
     * @param node look up result founder/sender
     * @param key looked up key
     * @param value value of the key
     */
    default void onKeyLookupResult(KademliaNode<ID, C> kademliaNode, Node<ID, C> node, K key, V value){}
    /**
     * Called when get store result is back to the node {@link KademliaRepositoryNode}
     * @param kademliaNode reference to the kademlia node that is calling the listener
     * @param node node that stored the data
     * @param key key of data
     */
    default void onKeyStoredResult(KademliaNode<ID, C> kademliaNode, Node<ID, C> node, K key, boolean success){}

    /**
     * Default implementation of {@link KademliaNodeListener} which does nothing
     * @param <ID> Number type of node ID between supported types
     * @param <C> Your implementation of connection info
     */
    class Default<ID extends Number, C extends ConnectionInfo> implements KademliaNodeListener<ID, C, Void, Void> {}
}
