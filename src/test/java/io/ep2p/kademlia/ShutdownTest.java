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

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *  Testing graceful shutdown and shutdown protocol
 */
class ShutdownTest {

    @Test
    void testGracefulShutdown() throws InterruptedException, ExecutionException {
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

        KademliaNodeAPI<Integer, EmptyConnectionInfo> node7 = null;
        // Other nodes
        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            KademliaNodeAPI<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, nodeSettings);
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
            if (i == 7){
                node7 = nextNode;
            }
        }

        
        // Test if nodes know about each other
        assert node7 != null;
        node7.stop();
        messageSenderAPI.map.remove(7);

        Thread.sleep((NodeSettings.Default.PING_SCHEDULE_TIME_VALUE + 1) * 1000L);

        boolean contains7 = containsNode(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(15)),7);

        Assertions.assertFalse(contains7, "Node 15 still knows about 7 even after 7 stopped");

        // stop all
        messageSenderAPI.stopAll();
    }

    @Test
    void testShutdown() throws InterruptedException, ExecutionException {
        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings nodeSettings = NodeSettings.Default.build();

        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);


        // Bootstrap Node
        KademliaNodeAPI<Integer, EmptyConnectionInfo> bootstrapNode = new KademliaNode<>(0, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(0), messageSenderAPI, nodeSettings);
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        KademliaNodeAPI<Integer, EmptyConnectionInfo> node7 = null;
        // Other nodes
        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            KademliaNodeAPI<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, nodeSettings);
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
            if (i == 7){
                node7 = nextNode;
            }
        }

        Thread.sleep(2000);

        // Test if nodes know about each other
        assert node7 != null;
        node7.stopNow();
        messageSenderAPI.map.remove(7);

        boolean contains7 = containsNode(new ClosestPerBucketReferencedNodeStrategy().getReferencedNodes(messageSenderAPI.map.get(15)),7);

        Assertions.assertTrue(contains7, "Node 15 already forgot about 7");

        // Wait for ping
        Thread.sleep(nodeSettings.getPingScheduleTimeValue() * 1100L);

        // Now 15 should not know 7
        contains7 = containsNode(new ClosestPerBucketReferencedNodeStrategy().getReferencedNodes(messageSenderAPI.map.get(15)),7);
        Assertions.assertFalse(contains7, "Node 15 still knows about 7 even after 7 ping");

        // stop all
        messageSenderAPI.stopAll();
    }

    private boolean containsNode(List<Node<Integer, EmptyConnectionInfo>> list, int nodeId){
        for (Node<Integer, EmptyConnectionInfo> referencedNode : list) {
            if(referencedNode.getId() == nodeId){
                return true;
            }
        }
        return false;
    }

}
