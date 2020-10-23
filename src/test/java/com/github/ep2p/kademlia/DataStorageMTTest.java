package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.connection.EmptyConnectionInfo;
import com.github.ep2p.kademlia.connection.LocalNodeConnectionApi;
import com.github.ep2p.kademlia.connection.LongRunningLocalNodeConnectionApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.exception.GetException;
import com.github.ep2p.kademlia.exception.StoreException;
import com.github.ep2p.kademlia.model.GetAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.node.*;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import com.github.ep2p.kademlia.table.SimpleRoutingTableFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DataStorageMTTest {

    @Test
    public void canStoreDataWhenCalledInMultipleThreads() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeConnectionApi nodeApi = new LocalNodeConnectionApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), routingTableFactory.getRoutingTable(0), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.start();


        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaRepositoryNode<EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i, routingTableFactory.getRoutingTable(i), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        AtomicReference<StoreAnswer<Integer>> atomicReference = new AtomicReference<>();
        for (int i = 0; i < 10; i++){
            Thread thread = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    try {
                        StoreAnswer<Integer> storeAnswer = node0.store(data.hashCode(), data);
                        atomicReference.set(storeAnswer);
                    }catch (StoreException e){
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            Thread.sleep(20);
        }
        Thread.sleep(3000);
        StoreAnswer<Integer> storeAnswer = atomicReference.get();
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) storeAnswer.getKey(), data.hashCode(), "StoreAnswer key was " + storeAnswer.getResult());
        System.out.println("Successfully stored `" + data +"` on node " + storeAnswer.getNodeId());

        Assertions.assertNull(node0.getKademliaRepository().get(data.hashCode()), "Invalid node is holding data");

        GetAnswer<Integer, String> getAnswer = node0.get(data.hashCode());
        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND, "GetAnswer Result was " + storeAnswer.getResult());
        Assertions.assertEquals((int) getAnswer.getKey(), data.hashCode(), "GetAnswer key was " + storeAnswer.getResult());
        Assertions.assertEquals(getAnswer.getValue(), data, "GetAnswer value was " + storeAnswer.getResult());

        System.out.println("Successfully retrieved `"+ data +"` from node " + getAnswer.getNodeId());

    }

    @Test
    public void getsValidTimeoutOnLongStore() throws BootstrapException, StoreException, InterruptedException, GetException {
        LongRunningLocalNodeConnectionApi nodeApi = new LongRunningLocalNodeConnectionApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        RoutingTableFactory<EmptyConnectionInfo, Integer> routingTableFactory = new SimpleRoutingTableFactory();
        Common.IDENTIFIER_SIZE = 4;
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 2;

        //bootstrap node
        KademliaSyncRepositoryNode<EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), routingTableFactory.getRoutingTable(0), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        nodeApi.registerNode(node0);
        node0.start();


        for(int i = 1; i < Math.pow(2, Common.IDENTIFIER_SIZE); i++){
            KademliaRepositoryNode<EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i, routingTableFactory.getRoutingTable(i), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<Integer> storeAnswer = node0.store(data.hashCode(), data, 100, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.TIMEOUT, "Could not get timeout from store answer");

    }


    public static void main(String[] args) {
        test(1L, TimeUnit.SECONDS);
        test(1L, TimeUnit.DAYS);
    }

    private static void test(long input, TimeUnit timeUnit){
        System.out.println(timeUnit.convert(input, TimeUnit.MILLISECONDS) + "");
        System.out.println(timeUnit.toMillis(input) + "");
        System.out.println("----");
    }


}
