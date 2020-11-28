package com.github.ep2p.kademlia.util;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

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

    public static synchronized <ID extends Number> List<ID> getNodesWithDistance(ID nodeId, int identifierSize) {
        if(nodeId instanceof Long){
            ArrayList<Long> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> {
                validNodes.add(((Long) nodeId) ^ distance);
            });

            return (List<ID>) validNodes;
        }
        return new ArrayList<>();
    }

    public static <ID extends Number, C extends ConnectionInfo> Node<ID, C> getClosest(List<Node<ID, C>> nodes, int distance){
        assert nodes.size() > 0;

        Node<ID, C> node = nodes.get(0);
        int nDistance = ((Number) nodes.get(0).getId()).byteValue() ^ distance;
        for (Node<ID, C> cNode : nodes) {
            int tempDistance = ((Number) cNode.getId()).byteValue() ^ distance;
            if(tempDistance < nDistance){
                nDistance = tempDistance;
                node = cNode;
            }
        }

        return node;
    }
}
