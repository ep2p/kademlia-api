package io.ep2p.kademlia;

import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaNodeListener;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.SimpleRoutingTableFactory;

import java.util.List;

public class NodeTableSize {

    public static void main(String[] args) throws BootstrapException, InterruptedException {
        LocalNodeConnectionApi nodeApi = new LocalNodeConnectionApi();
        Common.IDENTIFIER_SIZE = 9;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;
        SimpleRoutingTableFactory routingTableFactory = new SimpleRoutingTableFactory();

        KademliaNode<Integer, EmptyConnectionInfo> node0 = new KademliaNode<>(0, routingTableFactory.getRoutingTable(0), nodeApi, new EmptyConnectionInfo());
        nodeApi.registerNode(node0);
        node0.start();

        KademliaNode<Integer, EmptyConnectionInfo> lastNode = null;

        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaNode<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, routingTableFactory.getRoutingTable(i), nodeApi, new EmptyConnectionInfo());
            nodeApi.registerNode(nextNode);
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
        for (Bucket<Integer, EmptyConnectionInfo> bucket : lastNode.getRoutingTable().getBuckets()) {
            i += bucket.size();
        }

        System.out.println(i);

        i = 0;
        for (Bucket<Integer, EmptyConnectionInfo> bucket : node0.getRoutingTable().getBuckets()) {
            i += bucket.size();
        }

        System.out.println(i);

    }

}
