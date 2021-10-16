package example;

import java.util.ArrayList;

//More info: http://tutorials.jenkov.com/p2p/peer-routing-table.html
public class KademliaNodesToReference {
    public static void main(String[] args) {
        // Node identifier size
        int identifierSize = 4;

        // Valid distances according to identifier size
        ArrayList<Integer> distances = new ArrayList<>();
        for(int i = 0; i < identifierSize; i++){
            distances.add((int) Math.pow(2, i));
        }

        // Your node id here. Must be in range of 0 -> (2 power identifierSize)
        int nodeId = 15;

        // Extracting nodes with specified distance
        ArrayList<Integer> validNodes = new ArrayList<>();
        distances.forEach(distance -> validNodes.add(nodeId ^ distance));
        System.out.println(validNodes);
    }
}
