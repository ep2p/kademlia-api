package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.node.*;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;

import java.util.Arrays;
import java.util.List;

public class NodeTableSize {

    public static void main(String[] args) throws BootstrapException, InterruptedException {
        LocalNodeApi nodeApi = new LocalNodeApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 9;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;

        KademliaNode<EmptyConnectionInfo> node0 = new KademliaNode<>(nodeIdFactory.getNodeId(), routingTableFactory, nodeApi, new EmptyConnectionInfo());
        LocalNodeApi.registerNode(node0);
        node0.start();

        KademliaNode<EmptyConnectionInfo> lastNode = null;

        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaNode<EmptyConnectionInfo> nextNode = new KademliaNode<>(nodeIdFactory.getNodeId(), routingTableFactory, nodeApi, new EmptyConnectionInfo());
            LocalNodeApi.registerNode(nextNode);
            nextNode.bootstrap(node0);
            if(i == Math.pow(2, Common.IDENTIFIER_SIZE) - 1){
                lastNode = nextNode;
            }
        }
        lastNode.setKademliaNodeListener(new KademliaNodeListener() {
            @Override
            public void onReferencedNodesUpdate(KademliaNode kademliaNode, List referencedNodes) {
                System.out.println(referencedNodes);
            }
        });


        Thread.sleep(4000);

        int i = 0;
        for (Bucket<EmptyConnectionInfo> bucket : lastNode.getRoutingTable().getBuckets()) {
            i += bucket.size();
        }

        System.out.println(i);

        i = 0;
        for (Bucket<EmptyConnectionInfo> bucket : node0.getRoutingTable().getBuckets()) {
            i += bucket.size();
        }

        System.out.println(i);

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
