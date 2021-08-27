package io.ep2p.kademlia;

import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataStorageTest {

    //todo
    @Test
    public void canStoreDataInNetwork() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.start();

        Thread.sleep(100);

        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            Thread.sleep(100);
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<Integer, Integer> storeAnswer = node0.store(data.hashCode(), data, true);
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) storeAnswer.getKey(), data.hashCode(), "StoreAnswer key was " + storeAnswer.getResult());
        System.out.println("Successfully stored `" + data +"` on node " + storeAnswer.getNodeId());

        Assertions.assertNull(node0.getKademliaRepository().get(data.hashCode()), "Invalid node is holding data");

        GetAnswer<Integer,Integer, String> getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());

    }

    @Test
    public void canStoreWhenNetworkIsNotFull() throws InterruptedException, BootstrapException, StoreException, GetException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.start();


        for(int i = 1; i < (Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE) / 2); i++){
            KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i * 2, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<Integer, Integer> storeAnswer = node0.store(data.hashCode(), data, true);
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) storeAnswer.getKey(), data.hashCode(), "StoreAnswer key was " + storeAnswer.getResult());
        System.out.println("Successfully stored `" + data +"` on node " + storeAnswer.getNodeId());

        Assertions.assertNull(node0.getKademliaRepository().get(data.hashCode()), "Invalid node is holding data");

        GetAnswer<Integer,Integer, String> getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
    }

}
