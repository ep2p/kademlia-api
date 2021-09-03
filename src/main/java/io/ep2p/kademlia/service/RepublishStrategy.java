package io.ep2p.kademlia.service;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaRepositoryNode;
import io.ep2p.kademlia.node.TimestampAwareKademliaRepository;

public interface RepublishStrategy<ID extends Number, C extends ConnectionInfo, K, V> {

    /**
     * @param republishSettings settings for republish strategy
     * @param timestampAwareKademliaRepository kademlia repository with more query functionality
     */
    void configure(
            KademliaRepositoryNode<ID, C, K, V> kademliaRepositoryNode,
            NodeSettings.RepublishSettings republishSettings,
            TimestampAwareKademliaRepository<K, V> timestampAwareKademliaRepository
    );

    /**
     * Start automatic republishing
     * Implementation needs to keep running
     * Its better to choose running intervals based on republishSettings
     * Called when node constructs
     */
    void start();

    /**
     * Stop republishing
     * Mostly called at node shutting down
     */
    void stop();
}
