package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeApi;
import com.github.ep2p.kademlia.connection.NodeApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.node.IncrementalNodeIdFactory;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaNodeListener;
import com.github.ep2p.kademlia.node.NodeIdFactory;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NodesJoiningTest {

    @Test
    public void canPeersJoinNetwork() throws BootstrapException {
        LocalNodeApi nodeApi = new LocalNodeApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 5;

        KademliaNodeListener<EmptyConnectionInfo> listener = new KademliaNodeListener<EmptyConnectionInfo>() {
            @Override
            public void onReferencedNodesUpdate(KademliaNode kademliaNode, List referencedNodes) {
                System.out.println("Referenced nodes of "+ kademliaNode.getId() + " -> "+referencedNodes);
            }

            @Override
            public void onStartupComplete(KademliaNode<EmptyConnectionInfo> kademliaNode) {
                System.out.println("Node "+kademliaNode.getId()+" finished bootstrap");
            }
        };

        KademliaNode<EmptyConnectionInfo> node0 = new KademliaNode<>(nodeApi, nodeIdFactory, new EmptyConnectionInfo(), routingTableFactory);
        LocalNodeApi.registerNode(node0);
        node0.setKademliaNodeListener(listener);
        KademliaNode<EmptyConnectionInfo> node1 = new KademliaNode<>(nodeApi, nodeIdFactory, new EmptyConnectionInfo(), routingTableFactory);
        LocalNodeApi.registerNode(node1);
        node1.setKademliaNodeListener(listener);
        KademliaNode<EmptyConnectionInfo> node2 = new KademliaNode<>(nodeApi, nodeIdFactory, new EmptyConnectionInfo(), routingTableFactory);
        LocalNodeApi.registerNode(node2);
        node2.setKademliaNodeListener(listener);
        KademliaNode<EmptyConnectionInfo> node3 = new KademliaNode<>(nodeApi, nodeIdFactory, new EmptyConnectionInfo(), routingTableFactory);
        LocalNodeApi.registerNode(node3);
        node3.setKademliaNodeListener(listener);
        KademliaNode<EmptyConnectionInfo> node4 = new KademliaNode<>(nodeApi, nodeIdFactory, new EmptyConnectionInfo(), routingTableFactory);
        LocalNodeApi.registerNode(node4);
        node4.setKademliaNodeListener(listener);
        KademliaNode<EmptyConnectionInfo> node5 = new KademliaNode<>(nodeApi, nodeIdFactory, new EmptyConnectionInfo(), routingTableFactory);
        LocalNodeApi.registerNode(node5);
        node5.setKademliaNodeListener(listener);

        node0.start();
        node1.bootstrap(node0);
        node2.bootstrap(node0);
        node3.bootstrap(node0);
        node4.bootstrap(node0);
        node5.bootstrap(node0);

        node5.getRoutingTable().getBuckets().forEach(bucket -> {
            bucket.getNodeIds().forEach(System.out::println);
        });


        while (true){}
        //this is not a test yet ... just seeing how it works
    }

}
