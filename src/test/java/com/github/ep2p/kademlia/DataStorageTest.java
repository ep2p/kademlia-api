package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.exception.GetException;
import com.github.ep2p.kademlia.exception.StoreException;
import com.github.ep2p.kademlia.model.GetAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.node.*;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;
import com.github.ep2p.kademlia.util.BoundedHashUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataStorageTest {

    @Test
    public void canStoreDataInNetwork() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeApi nodeApi = new LocalNodeApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), routingTableFactory, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        LocalNodeApi.registerNode(node0);
        node0.start();


        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaRepositoryNode<EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i, routingTableFactory, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            LocalNodeApi.registerNode(aNode);
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

        System.out.println("Sucessfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());

    }

    public static void main(String[] args) {
        BoundedHashUtil boundedHashUtil = new BoundedHashUtil(4);
        String test1 = "hehehe";
        String test2 = "World";
        String test3 = "Whats";
        String test4 = "Upside down";
        String test5 = "Eleuth";
        String test6 = "EP2p";
        System.out.println(test1 + " " + boundedHashUtil.hash(test1.hashCode()));
        System.out.println(test2 + " " + boundedHashUtil.hash(test2.hashCode()));
        System.out.println(test3 + " " + boundedHashUtil.hash(test3.hashCode()));
        System.out.println(test4 + " " + boundedHashUtil.hash(test4.hashCode()));
        System.out.println(test5 + " " + boundedHashUtil.hash(test5.hashCode()));
        System.out.println(test6 + " " + boundedHashUtil.hash(test6.hashCode()));
    }

}
