package com.github.ep2p.kademlia;

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

    public static synchronized List<Integer> getNodesWithDistance(int nodeId, int identifierSize) {
        ArrayList<Integer> validNodes = new ArrayList<>();
        getDistancesOfIdentifierSize(identifierSize).forEach(distance -> {
            validNodes.add(nodeId ^ distance);
        });

        return validNodes;
    }
}
