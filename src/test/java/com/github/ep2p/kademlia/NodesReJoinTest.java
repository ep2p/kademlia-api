package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeConnectionApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.exception.ShutdownException;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaNodeListener;
import com.github.ep2p.kademlia.node.Node;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodesReJoinTest {

    @Test
    public void canPeersLeaveAndRejoin() throws BootstrapException, InterruptedException, ShutdownException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        SimpleRoutingTableFactory routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;

        Map<Integer, List<Node<Integer, EmptyConnectionInfo>>> map = new ConcurrentHashMap<>();

        KademliaNodeListener<Integer, EmptyConnectionInfo, Void, Void> listener = new KademliaNodeListener<Integer, EmptyConnectionInfo, Void, Void>() {
            @Override
            public void onReferencedNodesUpdate(KademliaNode kademliaNode, List referencedNodes) {
                map.put((Integer) kademliaNode.getId(), referencedNodes);
            }
        };

        KademliaNode<Integer, EmptyConnectionInfo> node0 = new KademliaNode<Integer, EmptyConnectionInfo>(0, routingTableFactory.getRoutingTable(0), nodeApi, new EmptyConnectionInfo());
        nodeApi.registerNode(node0);
        node0.setKademliaNodeListener(listener);
        node0.start();

        KademliaNode<Integer, EmptyConnectionInfo> node7 = null;

        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaNode<Integer, EmptyConnectionInfo> aNode = new KademliaNode<>(i, routingTableFactory.getRoutingTable(i), nodeApi, new EmptyConnectionInfo());
            nodeApi.registerNode(aNode);
            aNode.setKademliaNodeListener(listener);
            aNode.bootstrap(node0);
            if(i == 7)
                node7 = aNode;
        }


        while (map.size() <= Common.IDENTIFIER_SIZE){
            //wait
        }
        Thread.sleep((long)(1.1D * Common.REFERENCED_NODES_UPDATE_PERIOD_SEC * 1000L));

        Assertions.assertTrue(listContainsAll(map.get(15), 14,13,11,7));
        Assertions.assertTrue(listContainsAll(map.get(7), 6,5,3,15));
        System.out.println("7 and 15 know each other");

        //When node7 leaves network, node 15 should no longer hold reference to it
        assert node7 != null;
        node7.stop();
        Thread.sleep((long)(1.1D * Common.REFERENCED_NODES_UPDATE_PERIOD_SEC * 1000L));
        Assertions.assertTrue(listDoesntContain(map.get(15), 7));
        System.out.println("15 doesnt know 7 after it (7) left network");


        //When node7 comes back to network, node 15 should be informed and reference to it again
        //We have to recreate node 7, cause once a node shutsdown it cant start again (since executors shutdown too)
        KademliaNode<Integer, EmptyConnectionInfo> finalNode = node7;
        node7 = new KademliaNode<>(7, node7.getRoutingTable(), nodeApi, new EmptyConnectionInfo());
        node7.start();
        Thread.sleep((long)(1.1D * Common.REFERENCED_NODES_UPDATE_PERIOD_SEC * 1000L));
        Assertions.assertTrue(listContainsAll(map.get(15), 14,13,11,7));
        System.out.println("15 knows about 7 again after it (7) rejoined");
    }

    private boolean listDoesntContain(List<Node<Integer, EmptyConnectionInfo>> referencedNodes, Integer... nodeIds){
        List<Integer> nodeIdsToContain = Arrays.asList(nodeIds);
        for (Node<Integer, EmptyConnectionInfo> referencedNode : referencedNodes) {
            if(nodeIdsToContain.contains(referencedNode.getId()))
                return false;
        }
        return true;
    }

    private boolean listContainsAll(List<Node<Integer, EmptyConnectionInfo>> referencedNodes, Integer... nodeIds){
        List<Integer> nodeIdsToContain = Arrays.asList(nodeIds);
        for (Node<Integer, EmptyConnectionInfo> referencedNode : referencedNodes) {
            if(!nodeIdsToContain.contains(referencedNode.getId()))
                return false;
        }
        return true;
    }

}
