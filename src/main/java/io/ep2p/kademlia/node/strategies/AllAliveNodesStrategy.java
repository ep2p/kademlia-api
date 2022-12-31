package io.ep2p.kademlia.node.strategies;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllAliveNodesStrategy implements ReferencedNodesStrategy {
    @Override
    public <I extends Number, C extends ConnectionInfo> List<Node<I, C>> getReferencedNodes(KademliaNodeAPI<I, C> kademliaNode) {
        Date date = DateUtil.getDateOfSecondsAgo(kademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        List<Node<I, C>> referencedNodes = new ArrayList<>();

        kademliaNode.getRoutingTable().getBuckets().forEach(bucket -> bucket.getNodeIds().forEach(id -> {
            ExternalNode<I, C> node = bucket.getNode(id);
            if (node.getLastSeen().after(date)){
                referencedNodes.add(node);
            }
        }));

        return referencedNodes;
    }
}
