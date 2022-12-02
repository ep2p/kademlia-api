package io.ep2p.kademlia;

import io.ep2p.kademlia.helpers.EmptyConnectionInfo;
import io.ep2p.kademlia.helpers.TestMessageSenderAPI;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.strategies.ClosestPerBucketReferencedNodeStrategy;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *  Basic test to make sure network can initialize by bootstrapping and nodes find each other
 */
class NodesJoiningTest {

    @Test
    void canPeersJoinNetwork() throws InterruptedException, ExecutionException {
        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings nodeSettings = NodeSettings.Default.build();
        ClosestPerBucketReferencedNodeStrategy closestPerBucketReferencedNodeStrategy = new ClosestPerBucketReferencedNodeStrategy();

        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);


        // Bootstrap Node
        KademliaNodeAPI<Integer, EmptyConnectionInfo> bootstrapNode = new KademliaNode<>(0, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(0), messageSenderAPI, nodeSettings);
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        // Other nodes
        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            KademliaNodeAPI<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, nodeSettings);
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
        }

        Thread.sleep(2000);

        // Test if nodes know about each other

        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(0)), 1,2,4,8));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(1)), 0,3,5,9));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(2)), 3,0,6,10));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(3)), 2,1,7,11));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(15)), 14,13,11,7));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(7)), 6,5,3,15));


        // stop all
        messageSenderAPI.stopAll();
    }

    private boolean listContainsAll(List<Node<Integer, EmptyConnectionInfo>> referencedNodes, Integer... nodeIds){
        List<Integer> nodeIdsToContain = new ArrayList<>(Arrays.asList(nodeIds));
        for (Node<Integer, EmptyConnectionInfo> referencedNode : referencedNodes) {
            nodeIdsToContain.remove(referencedNode.getId());
        }
        return nodeIdsToContain.size() == 0;
    }

}
