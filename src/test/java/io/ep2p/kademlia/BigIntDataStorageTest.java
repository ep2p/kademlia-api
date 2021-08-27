package io.ep2p.kademlia;

import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.KademliaRepositoryNode;
import io.ep2p.kademlia.node.KademliaSyncRepositoryNode;
import io.ep2p.kademlia.node.SampleRepository;
import io.ep2p.kademlia.util.BoundedHashUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;


//todo
public class BigIntDataStorageTest {

    @Test
    public void canStoreDataInNetwork() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeConnectionApi<BigInteger> nodeApi = new LocalNodeConnectionApi<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<BigInteger, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(BigInteger.valueOf(0), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.start();


        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            KademliaRepositoryNode<BigInteger, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(BigInteger.valueOf(i), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<BigInteger, Integer> storeAnswer = node0.store(data.hashCode(), data, true);
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) storeAnswer.getKey(), data.hashCode(), "StoreAnswer key was " + storeAnswer.getResult());
        System.out.println("Successfully stored `" + data +"` on node " + storeAnswer.getNodeId());

        Assertions.assertNull(node0.getKademliaRepository().get(data.hashCode()), "Invalid node is holding data");

        GetAnswer<BigInteger,Integer, String> getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());

    }

    @Test
    public void canStoreWhenNetworkIsNotFull() throws InterruptedException, BootstrapException, StoreException, GetException {
        LocalNodeConnectionApi<BigInteger> nodeApi = new LocalNodeConnectionApi<>();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<BigInteger, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(BigInteger.valueOf(0), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.start();


        for(int i = 1; i < (Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE) / 2); i++){
            KademliaRepositoryNode<BigInteger, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(BigInteger.valueOf(i), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<BigInteger, Integer> storeAnswer = node0.store(data.hashCode(), data, true);
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) storeAnswer.getKey(), data.hashCode(), "StoreAnswer key was " + storeAnswer.getResult());
        System.out.println("Successfully stored `" + data +"` on node " + storeAnswer.getNodeId());

        Assertions.assertNull(node0.getKademliaRepository().get(data.hashCode()), "Invalid node is holding data");

        GetAnswer<BigInteger,Integer, String> getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());
    }

    public static void main(String[] args) {
        BoundedHashUtil boundedHashUtil = new BoundedHashUtil(4);
        String test1 = "hehehe";
        String test2 = "World";
        String test3 = "Whats";
        String test4 = "Upside down";
        String test5 = "Eleuth";
        String test6 = "EP2p";
        String test7 = "3";
        System.out.println(test1 + " " + boundedHashUtil.hash(test1.hashCode(), Integer.class));
        System.out.println(test2 + " " + boundedHashUtil.hash(test2.hashCode(), Integer.class));
        System.out.println(test3 + " " + boundedHashUtil.hash(test3.hashCode(), Integer.class));
        System.out.println(test4 + " " + boundedHashUtil.hash(test4.hashCode(), Integer.class));
        System.out.println(test5 + " " + boundedHashUtil.hash(test5.hashCode(), Integer.class));
        System.out.println(test6 + " " + boundedHashUtil.hash(test6.hashCode(), Integer.class));
        System.out.println(test7 + " " + boundedHashUtil.hash(test7.hashCode(), Integer.class));
    }

}
