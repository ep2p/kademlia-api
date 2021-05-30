package io.ep2p.kademlia;

import io.ep2p.kademlia.node.*;
import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.node.*;
import io.ep2p.kademlia.table.SimpleRoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodesJoiningTest {

    @Test
    public void canPeersJoinNetwork() throws BootstrapException, InterruptedException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        SimpleRoutingTableFactory routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 5;

        Map<Integer, List<Node<Integer, EmptyConnectionInfo>>> map = new ConcurrentHashMap<>();

        KademliaNodeListener<Integer, EmptyConnectionInfo, Void, Void> listener = new KademliaNodeListener<Integer, EmptyConnectionInfo, Void, Void>() {
            @Override
            public void onReferencedNodesUpdate(KademliaNode kademliaNode, List referencedNodes) {
                map.put((Integer) kademliaNode.getId(), referencedNodes);
            }
        };

        KademliaNode<Integer, EmptyConnectionInfo> node0 = new KademliaNode<>(nodeIdFactory.getNodeId(), routingTableFactory.getRoutingTable(0), nodeApi, new EmptyConnectionInfo());
        nodeApi.registerNode(node0);
        node0.setKademliaNodeListener(listener);
        node0.start();

        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaNode<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, routingTableFactory.getRoutingTable(i), nodeApi, new EmptyConnectionInfo());
            nodeApi.registerNode(nextNode);
            nextNode.setKademliaNodeListener(listener);
            nextNode.bootstrap(node0);
        }


        while (map.size() <= Common.IDENTIFIER_SIZE){
            //wait
        }
        Thread.sleep((long)(1.4D * Common.REFERENCED_NODES_UPDATE_PERIOD_SEC * 1000L));

        Assertions.assertTrue(listContainsAll(map.get(0), 1,2,4,8));
        Assertions.assertTrue(listContainsAll(map.get(1), 0,3,5,9));
        Assertions.assertTrue(listContainsAll(map.get(2), 3,0,6,10));
        Assertions.assertTrue(listContainsAll(map.get(3), 2,1,7,11));
        Assertions.assertTrue(listContainsAll(map.get(15), 14,13,11,7));
        Assertions.assertTrue(listContainsAll(map.get(7), 6,5,3,15));
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
