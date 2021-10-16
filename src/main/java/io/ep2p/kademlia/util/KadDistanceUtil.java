package io.ep2p.kademlia.util;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KadDistanceUtil {
    private final static Map<Integer, List<Integer>> identifierToDistanceListMap = new HashMap<>();

    public static synchronized List<Integer> getDistancesOfIdentifierSize(int identifierSize){
        if(identifierToDistanceListMap.containsKey(identifierSize)){
            return identifierToDistanceListMap.get(identifierSize);
        }
        ArrayList<Integer> distances = new ArrayList<>();
        for(int i = 0; i < identifierSize; i++){
            distances.add((int) Math.pow(2, i));
        }
        identifierToDistanceListMap.put(identifierSize, distances);
        return distances;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <ID extends Number> List<ID> getNodesWithDistance(ID nodeId, int identifierSize) {
        if(nodeId instanceof Long){
            ArrayList<Long> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> validNodes.add(((Long) nodeId) ^ distance));

            return (List<ID>) validNodes;
        }
        if (nodeId instanceof BigInteger){
            ArrayList<BigInteger> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> validNodes.add(((BigInteger) nodeId).xor(BigInteger.valueOf(distance))));

            return (List<ID>) validNodes;
        }
        if (nodeId instanceof Integer){
            ArrayList<Integer> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> validNodes.add(((Integer) nodeId) ^ distance));

            return (List<ID>) validNodes;
        }

        return new ArrayList<>();
    }

    public static <ID extends Number, C extends ConnectionInfo> Node<ID, C> getClosest(List<Node<ID, C>> nodes, int distance){
        assert nodes.size() > 0;

        Node<ID, C> node = nodes.get(0);
        int nDistance = nodes.get(0).getId().byteValue() ^ distance;
        for (Node<ID, C> cNode : nodes) {
            int tempDistance = cNode.getId().byteValue() ^ distance;
            if(tempDistance < nDistance){
                nDistance = tempDistance;
                node = cNode;
            }
        }

        return node;
    }

    public static <ID extends Number, C extends ConnectionInfo> List<Node<ID, C>> getReferencedNodes(KademliaNodeAPI<ID, C> kademliaNodeAPI){
        List<Node<ID, C>> referencedNodes = new ArrayList<>();

        List<ID> distances = KadDistanceUtil.getNodesWithDistance(kademliaNodeAPI.getId(), kademliaNodeAPI.getNodeSettings().getIdentifierSize());
        distances.forEach(distance -> {
            FindNodeAnswer<ID, C> findNodeAnswer = kademliaNodeAPI.getRoutingTable().findClosest(distance);
            if (findNodeAnswer.getNodes().size() > 0) {
                if(!findNodeAnswer.getNodes().get(0).getId().equals(kademliaNodeAPI.getId()) && !referencedNodes.contains(findNodeAnswer.getNodes().get(0)))
                    referencedNodes.add(findNodeAnswer.getNodes().get(0));
            }
        });

        return referencedNodes;
    }
}
