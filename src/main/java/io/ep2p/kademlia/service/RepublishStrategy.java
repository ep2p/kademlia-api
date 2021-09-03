package io.ep2p.kademlia.service;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.node.TimestampAwareKademliaRepository;

public interface RepublishStrategy {

    /**
     * @param republishSettings settings for republish strategy
     * @param timestampAwareKademliaRepository kademlia repository with more query functionality
     */
    void configure(
            NodeSettings.RepublishSettings republishSettings,
            TimestampAwareKademliaRepository<?, ?> timestampAwareKademliaRepository
    );

    /**
     * Start automatic republishing
     * Implementation needs to keep running
     * Its better to choose running intervals based on republishSettings
     * Called when node constructs
     */
    void run();

    /**
     * Stop republishing
     * Mostly called at node shutting down
     */
    void stop();
}
