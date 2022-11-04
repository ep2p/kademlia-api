package io.ep2p.kademlia.node.strategies;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.util.KadDistanceUtil;

import java.util.ArrayList;
import java.util.List;

public class ClosestPerBucketReferencedNodeStrategy implements ReferencedNodesStrategy {
    @Override
    public <ID extends Number, C extends ConnectionInfo> List<Node<ID, C>> getReferencedNodes(KademliaNodeAPI<ID, C> kademliaNodeAPI) {
        List<Node<ID, C>> referencedNodes = new ArrayList<>();

        List<ID> distances = KadDistanceUtil.getNodesWithDistance(kademliaNodeAPI.getId(), kademliaNodeAPI.getNodeSettings().getIdentifierSize());
        distances.forEach(distance -> {
            FindNodeAnswer<ID, C> findNodeAnswer = kademliaNodeAPI.getRoutingTable().findClosest(distance);
            if (findNodeAnswer.getNodes().isEmpty()) {
                return;
            }
            if (!findNodeAnswer.getNodes().get(0).getId().equals(kademliaNodeAPI.getId()) && !referencedNodes.contains(findNodeAnswer.getNodes().get(0)))
                referencedNodes.add(findNodeAnswer.getNodes().get(0));
        });

        return referencedNodes;
    }
}
