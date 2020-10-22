package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.node.*;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodesJoiningTest {

    @Test
    public void canPeersJoinNetwork() throws BootstrapException, InterruptedException {
        LocalNodeApi nodeApi = new LocalNodeApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 5;

        Map<Integer, List<Node<EmptyConnectionInfo>>> map = new ConcurrentHashMap<>();

        KademliaNodeListener<EmptyConnectionInfo> listener = new KademliaNodeListener<EmptyConnectionInfo>() {
            @Override
            public void onReferencedNodesUpdate(KademliaNode kademliaNode, List referencedNodes) {
                map.put(kademliaNode.getId(), referencedNodes);
            }
        };

        KademliaNode<EmptyConnectionInfo> node0 = new KademliaNode<>(nodeIdFactory.getNodeId(), routingTableFactory, nodeApi, new EmptyConnectionInfo());
        LocalNodeApi.registerNode(node0);
        node0.setKademliaNodeListener(listener);
        node0.start();

        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaNode<EmptyConnectionInfo> nextNode = new KademliaNode<>(nodeIdFactory.getNodeId(), routingTableFactory, nodeApi, new EmptyConnectionInfo());
            LocalNodeApi.registerNode(nextNode);
            nextNode.setKademliaNodeListener(listener);
            nextNode.bootstrap(node0);
        }


        while (map.size() <= Common.IDENTIFIER_SIZE){
            //wait
        }
        Thread.sleep((long)(1.1D * Common.REFERENCED_NODES_UPDATE_PERIOD_SEC * 1000L));

        Assertions.assertTrue(listContainsAll(map.get(0), 1,2,4,8));
        Assertions.assertTrue(listContainsAll(map.get(1), 0,3,5,9));
        Assertions.assertTrue(listContainsAll(map.get(2), 3,0,6,10));
        Assertions.assertTrue(listContainsAll(map.get(3), 2,1,7,11));
        Assertions.assertTrue(listContainsAll(map.get(15), 14,13,11,7));
        Assertions.assertTrue(listContainsAll(map.get(7), 6,5,3,15));

    }

    private boolean listContainsAll(List<Node<EmptyConnectionInfo>> referencedNodes, Integer... nodeIds){
        List<Integer> nodeIdsToContain = Arrays.asList(nodeIds);
        for (Node<EmptyConnectionInfo> referencedNode : referencedNodes) {
            if(!nodeIdsToContain.contains(referencedNode.getId()))
                return false;
        }
        return true;
    }

}
