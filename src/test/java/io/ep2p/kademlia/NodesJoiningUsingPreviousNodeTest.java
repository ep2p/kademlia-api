package io.ep2p.kademlia;

import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaNodeListener;
import io.ep2p.kademlia.node.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//uses previous node to join network instead of node 0
public class NodesJoiningUsingPreviousNodeTest {

    @Test
    public void canPeersJoinNetwork() throws BootstrapException, InterruptedException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 5;

        Map<Integer, List<Node<Integer, EmptyConnectionInfo>>> map = new ConcurrentHashMap<>();

        KademliaNodeListener<Integer, EmptyConnectionInfo, Void, Void> listener = new KademliaNodeListener<Integer, EmptyConnectionInfo, Void, Void>() {
            @Override
            public void onReferencedNodesUpdate(KademliaNode kademliaNode, List referencedNodes) {
                map.put((Integer) kademliaNode.getId(), referencedNodes);
            }
        };

        KademliaNode<Integer, EmptyConnectionInfo> previousNode = new KademliaNode<>(0, nodeApi, new EmptyConnectionInfo());
        nodeApi.registerNode(previousNode);
        previousNode.setKademliaNodeListener(listener);
        previousNode.start();

        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            KademliaNode<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, nodeApi, new EmptyConnectionInfo());
            nodeApi.registerNode(nextNode);
            nextNode.setKademliaNodeListener(listener);
            nextNode.bootstrap(previousNode);
            previousNode = nextNode;
        }


        while (map.size() < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE)){
            //wait
        }

        Assertions.assertTrue(listContainsAll(map.get(0), 1,2,4,8));
        Assertions.assertTrue(listContainsAll(map.get(1), 0,3,5,9));
        Assertions.assertTrue(listContainsAll(map.get(2), 4,3,0,6,10));
        Assertions.assertTrue(listContainsAll(map.get(3), 4,2,1,7,11));
        Assertions.assertTrue(listContainsAll(map.get(15), 14,13,11,7));
        Assertions.assertTrue(listContainsAll(map.get(7), 8,6,5,3,15));

        nodeApi.stopAll();
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
