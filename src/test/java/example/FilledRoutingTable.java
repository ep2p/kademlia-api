package example;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.helpers.EmptyConnectionInfo;
import io.ep2p.kademlia.helpers.TestMessageSenderAPI;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.kademlia.table.RoutingTableFactory;

public class FilledRoutingTable {
    public static void main(String[] args) throws FullBucketException {
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();

        int nodeId = 15;
        RoutingTable<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> mainRoutingTable = routingTableFactory.getRoutingTable(nodeId);

        for (int i = 0; i < Math.pow(NodeSettings.Default.IDENTIFIER_SIZE, 2); i++){
            KademliaNodeAPI<Integer, EmptyConnectionInfo> aNode = new KademliaNode(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), new TestMessageSenderAPI<>(), NodeSettings.Default.build());
            mainRoutingTable.update(aNode);
        }

        mainRoutingTable.getBuckets().forEach(bucket -> {
            System.out.println("Bucket [" + bucket.getId() +"] -> " + bucket.getNodeIds());
        });

    }
}
