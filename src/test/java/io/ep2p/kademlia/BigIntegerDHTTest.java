package io.ep2p.kademlia;

import io.ep2p.kademlia.helpers.*;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.node.builder.DHTKademliaNodeBuilder;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class BigIntegerDHTTest {

    @Test
    void testRandomStore() throws ExecutionException, InterruptedException {
        TestMessageSenderAPI<BigInteger, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 128;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 2;
        NodeSettings.Default.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = false;
        NodeSettings nodeSettings = NodeSettings.Default.build();


        RoutingTableFactory<BigInteger, EmptyConnectionInfo, Bucket<BigInteger, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);

        KeyHashGenerator<BigInteger, BigInteger> keyHashGenerator = key -> BigInteger.valueOf(1);


        DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> previousNode = null;
        for(int i = 1; i < 8; i++){
            BigInteger id = BigInteger.valueOf(new Random().nextInt((int) Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE)));
            DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> nextNode = new DHTKademliaNodeBuilder<>(id, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(id), messageSenderAPI, keyHashGenerator, new SampleBigIntegerRepository()).build();
            messageSenderAPI.registerNode(nextNode);
            if (previousNode != null)
                Assertions.assertTrue(nextNode.start(previousNode).get(), "Failed to bootstrap the node with ID " + i);
            else
                nextNode.start();
            previousNode = nextNode;
        }
        System.out.println("Started all nodes.");

        Thread.sleep(5000);

        messageSenderAPI.map.forEach((bigInteger, kademliaNodeAPI) -> {
            try {
                StoreAnswer<BigInteger, EmptyConnectionInfo, BigInteger> storeAnswer = ((DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String>) kademliaNodeAPI).store(kademliaNodeAPI.getId(), kademliaNodeAPI.getId().toString()).get();
                Assertions.assertEquals(StoreAnswer.Result.STORED, storeAnswer.getResult());
                System.out.println("["+storeAnswer.getNode().getId()+"] stored " + storeAnswer.getKey());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        messageSenderAPI.map.forEach((bigInteger, kademliaNodeAPI) -> messageSenderAPI.map.keySet().forEach(key -> {
            try {
                LookupAnswer<BigInteger, EmptyConnectionInfo, BigInteger, String> lookupAnswer = ((DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String>) kademliaNodeAPI).lookup(key).get(5, TimeUnit.SECONDS);
                Assertions.assertEquals(LookupAnswer.Result.FOUND, lookupAnswer.getResult(), kademliaNodeAPI.getId() + " couldn't find key " + key);
                System.out.println("Requester: " + kademliaNodeAPI.getId() + " - Key: " + key + " - Owner: " + lookupAnswer.getNode().getId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }));
    }

    @Test
    void testStore() throws ExecutionException, InterruptedException {
        TestMessageSenderAPI<BigInteger, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 32;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings.Default.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = false;
        NodeSettings nodeSettings = NodeSettings.Default.build();

        RoutingTableFactory<BigInteger, EmptyConnectionInfo, Bucket<BigInteger, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);

        KeyHashGenerator<BigInteger, BigInteger> keyHashGenerator = new SampleBigIntegerKeyHashGenerator(NodeSettings.Default.IDENTIFIER_SIZE);

        // Bootstrap Node
        DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> bootstrapNode = new DHTKademliaNodeBuilder<>(BigInteger.valueOf(0), new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(BigInteger.valueOf(0)), messageSenderAPI, keyHashGenerator, new SampleRepository<>()).build();
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        // Other nodes
        for(int i = 1; i < 30; i++){
            BigInteger id = BigInteger.valueOf(i);
            DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> nextNode = new DHTKademliaNodeBuilder<>(id, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(id), messageSenderAPI, keyHashGenerator, new SampleRepository<>()).build();
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
        }


        Thread.sleep(2000);

        for (int i = 0; i < 10; i++){
            testStore(bootstrapNode, UUID.randomUUID().toString());
        }

        String data2 = UUID.randomUUID().toString();
        Assertions.assertThrows(TimeoutException.class, () -> bootstrapNode.store(BigInteger.valueOf(data2.hashCode()), data2).get(1, TimeUnit.NANOSECONDS));

        messageSenderAPI.stopAll();
    }

    private void testStore(DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> node, String data) throws ExecutionException, InterruptedException {
        Future<StoreAnswer<BigInteger, EmptyConnectionInfo, BigInteger>> storeFuture = node.store(BigInteger.valueOf(data.hashCode()), data);
        StoreAnswer<BigInteger, EmptyConnectionInfo, BigInteger> storeAnswer = storeFuture.get();
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult() + ", stored in node" + storeAnswer.getNode().getId());
        System.out.println(storeAnswer.getNode().getId() + " stored " + storeAnswer.getKey());

        if (!storeAnswer.getNode().getId().equals(node.getId()))
            Assertions.assertFalse(node.getKademliaRepository().contains(BigInteger.valueOf(data.hashCode())));

        Future<LookupAnswer<BigInteger, EmptyConnectionInfo, BigInteger, String>> lookupFuture = node.lookup(BigInteger.valueOf(data.hashCode()));
        LookupAnswer<BigInteger, EmptyConnectionInfo, BigInteger, String> lookupAnswer = lookupFuture.get();

        Assertions.assertEquals(lookupAnswer.getResult(), LookupAnswer.Result.FOUND);
        Assertions.assertEquals(lookupAnswer.getValue(), data);
        Assertions.assertEquals(lookupAnswer.getNode().getId(), storeAnswer.getNode().getId());
        System.out.println(lookupAnswer.getNode().getId() + " returned the data");
    }

}
