package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeConnectionApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.exception.GetException;
import com.github.ep2p.kademlia.exception.StoreException;
import com.github.ep2p.kademlia.model.GetAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.node.*;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//This can be an example of redistributing data when valid holder comes back online
public class DataStorageReDistributionTest {

    @Test
    public void canStoreDataInNetwork() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeConnectionApi nodeApi = new LocalNodeConnectionApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;

        KademliaNodeListener<EmptyConnectionInfo, Integer, String> redistributionKademliaNodeListener = new RedistributionKademliaNodeListener<EmptyConnectionInfo, Integer, String>();

        //bootstrap node
        KademliaSyncRepositoryNode<EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), routingTableFactory.getRoutingTable(0), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        LocalNodeConnectionApi.registerNode(node0);
        node0.setKademliaNodeListener(redistributionKademliaNodeListener);
        node0.start();




        for(int i = 1; i < (Math.pow(2, Common.IDENTIFIER_SIZE) / 2); i++){
            KademliaRepositoryNode<EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i * 2, routingTableFactory.getRoutingTable(i*2), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            LocalNodeConnectionApi.registerNode(aNode);
            aNode.setKademliaNodeListener(redistributionKademliaNodeListener);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<Integer> storeAnswer = node0.store(data.hashCode(), data);
        Assertions.assertEquals(storeAnswer.getAction(), StoreAnswer.Action.STORED, "StoreAnswer Action was " + storeAnswer.getAction());
        Assertions.assertEquals((int) storeAnswer.getKey(), data.hashCode(), "StoreAnswer key was " + storeAnswer.getAction());
        System.out.println("Successfully stored `" + data +"` on node " + storeAnswer.getNodeId());

        Assertions.assertNull(node0.getKademliaRepository().get(data.hashCode()), "Invalid node is holding data");

        GetAnswer<Integer, String> getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getAction(), GetAnswer.Action.FOUND, "GetAnswer Action was " + storeAnswer.getAction());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getAction());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getAction());
        Assertions.assertEquals((int) getAnswer.getNodeId(), 10, "Holder node id was " + storeAnswer.getNodeId());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
        System.out.println("Making node 11 and checking if data re-distributes");

        KademliaRepositoryNode<EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(11, routingTableFactory.getRoutingTable(11), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        LocalNodeConnectionApi.registerNode(aNode);
        aNode.bootstrap(node0);

        Thread.sleep(2000);
        getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getAction(), GetAnswer.Action.FOUND, "GetAnswer Action was " + storeAnswer.getAction());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getAction());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getAction());
        Assertions.assertEquals((int) getAnswer.getNodeId(), 11, "Holder node id was " + storeAnswer.getNodeId());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
        Thread.sleep(1000);
    }

}
