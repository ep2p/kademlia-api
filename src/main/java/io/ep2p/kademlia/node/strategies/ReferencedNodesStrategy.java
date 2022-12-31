package io.ep2p.kademlia.node.strategies;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import lombok.Getter;

import java.util.List;

/**
 *  ReferencedNodesStrategy returns list of nodes that should be pinged in a scheduler and also shutdown signal should be sent to
 */
public interface ReferencedNodesStrategy {
    <I extends Number, C extends ConnectionInfo> List<Node<I, C>> getReferencedNodes(KademliaNodeAPI<I, C> kademliaNode);

    enum Strategies {
        CLOSEST_PER_BUCKET(new ClosestPerBucketReferencedNodeStrategy()), EMPTY(new EmptyReferencedNodeStrategy()), ALL_ALIVE(new AllAliveNodesStrategy());

        @Getter
        private final ReferencedNodesStrategy referencedNodesStrategy;

        Strategies(ReferencedNodesStrategy referencedNodesStrategy) {
            this.referencedNodesStrategy = referencedNodesStrategy;
        }
    }
}
