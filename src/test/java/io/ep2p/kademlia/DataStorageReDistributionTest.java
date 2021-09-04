package io.ep2p.kademlia;

import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.ShutdownException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//This can be an example of redistributing data when valid holder comes back online
public class DataStorageReDistributionTest {

    @Test
    public void canRedistributeDataOnNodeAppear() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;

        KademliaNodeListener<Integer, EmptyConnectionInfo, Integer, String> redistributionKademliaNodeListener = new RedistributionKademliaNodeListener<Integer, EmptyConnectionInfo, Integer, String>();

        //bootstrap node
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.setKademliaNodeListener(redistributionKademliaNodeListener);
        node0.start();




        for(int i = 1; i < (Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE) / 2); i++){
            KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i * 2, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.setKademliaNodeListener(redistributionKademliaNodeListener);
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
        Assertions.assertEquals((int) getAnswer.getNodeId(), 10, "Holder node id was " + storeAnswer.getNodeId());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
        System.out.println("Making node 11 and checking if data re-distributes");

        KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(11, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(aNode);
        aNode.bootstrap(node0);

        Thread.sleep(2000);
        getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getNodeId(), 11, "Holder node id was " + storeAnswer.getNodeId());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
        Thread.sleep(1000);

        nodeApi.stopAll();
    }


    @Test
    public void canRedistributeDataOnShutdown() throws BootstrapException, StoreException, InterruptedException, GetException, ShutdownException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;

        KademliaNodeListener<Integer, EmptyConnectionInfo, Integer, String> redistributionKademliaNodeListener = new RedistributionKademliaNodeListener<Integer, EmptyConnectionInfo, Integer, String>(true, new RedistributionKademliaNodeListener.ShutdownDistributionListener<Integer, EmptyConnectionInfo>() {
            @Override
            public void onFinish(KademliaNode<Integer, EmptyConnectionInfo> kademliaNode) {
                System.out.println("Finished redistributing data on shutdown.");
            }
        });

        //bootstrap node
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.setKademliaNodeListener(redistributionKademliaNodeListener);
        node0.start();


        for(int i = 1; i < (Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE) / 2); i++){
            KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i * 2, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.setKademliaNodeListener(redistributionKademliaNodeListener);
            aNode.bootstrap(node0);
        }

        KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node11 = new KademliaRepositoryNode<>(11, nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        node11.setKademliaNodeListener(redistributionKademliaNodeListener);
        nodeApi.registerNode(node11);
        node11.bootstrap(node0);

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
        Assertions.assertEquals((int) getAnswer.getNodeId(), 11, "Holder node id was " + storeAnswer.getNodeId());
        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
        System.out.println("Shutting down node 11 and checking if data re-distributes");
        node11.stop();

        Thread.sleep(5000);
        getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getNodeId(), 10, "Holder node id was " + storeAnswer.getNodeId());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
        Thread.sleep(1000);

        nodeApi.stopAll();
    }

}
