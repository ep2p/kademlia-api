package io.ep2p.kademlia.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KadDistanceUtil {
    private static final Map<Integer, List<Integer>> identifierToDistanceListMap = new HashMap<>();

    private KadDistanceUtil(){}

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
    public static <I extends Number> List<I> getNodesWithDistance(I nodeId, int identifierSize) {
        if(nodeId instanceof Long){
            ArrayList<Long> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> validNodes.add(((Long) nodeId) ^ distance));

            return (List<I>) validNodes;
        }
        if (nodeId instanceof BigInteger){
            ArrayList<BigInteger> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> validNodes.add(((BigInteger) nodeId).xor(BigInteger.valueOf(distance))));

            return (List<I>) validNodes;
        }
        if (nodeId instanceof Integer){
            ArrayList<Integer> validNodes = new ArrayList<>();
            getDistancesOfIdentifierSize(identifierSize).forEach(distance -> validNodes.add(((Integer) nodeId) ^ distance));

            return (List<I>) validNodes;
        }

        return new ArrayList<>();
    }
}
