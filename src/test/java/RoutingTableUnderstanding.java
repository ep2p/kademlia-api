import com.github.ep2p.kademlia.FindNodeAnswer;
import com.github.ep2p.kademlia.RoutingTable;

public class RoutingTableUnderstanding {
    public static void main(String[] args) {
        RoutingTable routingTable = new RoutingTable(1);
        for(int i = 0; i < 64; i++){
            routingTable.update(i);
        }
        printNodes(routingTable);
        System.out.println("--- --- ---");

        FindNodeAnswer findNodeAnswer = routingTable.findClosest(3);
        System.out.println("Results for closest nodes to 3: ");
        findNodeAnswer.getNodes().forEach(contact -> {
            System.out.println(contact.getId());
        });
    }

    private static void printNodes(RoutingTable routingTable){
        routingTable.getBuckets().forEach(bucket -> {
            if (bucket.getNodes().size() > 0) {
                System.out.println("Nodes of bucket with id " + bucket.getId() + " -> " + Integer.toBinaryString(bucket.getId()));
                bucket.getNodes().forEach(nodeId -> {
                    System.out.println(nodeId + " -> " + Integer.toBinaryString(nodeId));
                });
            }
        });
    }
}
