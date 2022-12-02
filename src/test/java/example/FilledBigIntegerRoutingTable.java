package example;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.helpers.EmptyConnectionInfo;
import io.ep2p.kademlia.helpers.TestMessageSenderAPI;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.table.BigIntegerBucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.kademlia.table.RoutingTableFactory;

import java.math.BigInteger;


public class FilledBigIntegerRoutingTable {
    public static void main(String[] args) throws FullBucketException {
        NodeSettings.Default.IDENTIFIER_SIZE = 3;
        RoutingTableFactory<BigInteger, EmptyConnectionInfo, BigIntegerBucket<EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();
        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();
        BigInteger nodeId = BigInteger.valueOf(4);

        RoutingTable<BigInteger, EmptyConnectionInfo, BigIntegerBucket<EmptyConnectionInfo>> routingTable = routingTableFactory.getRoutingTable(nodeId);

        for (int i = 0; i < Math.pow(NodeSettings.Default.IDENTIFIER_SIZE, 2) - 1; i++){
            if (BigInteger.valueOf(i).equals(nodeId))
                continue;
            KademliaNodeAPI<BigInteger, EmptyConnectionInfo> node = new KademliaNode(
                    BigInteger.valueOf(i), new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(BigInteger.valueOf(i)), messageSenderAPI, NodeSettings.Default.build()
            );
            routingTable.update(node);
        }

        routingTable.getBuckets().forEach(bucket -> {
            System.out.println("Bucket [" + bucket.getId() + "] -> " + bucket.getNodeIds());
        });

    }
}
