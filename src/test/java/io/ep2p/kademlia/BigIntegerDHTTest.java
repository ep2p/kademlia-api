package io.ep2p.kademlia;

import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.helpers.*;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNode;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class BigIntegerDHTTest {

    @Test
    void testStore() throws ExecutionException, InterruptedException, DuplicateStoreRequest {
        TestMessageSenderAPI<BigInteger, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 32;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings.Default.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = false;
        NodeSettings nodeSettings = NodeSettings.Default.build();

        RoutingTableFactory<BigInteger, EmptyConnectionInfo, Bucket<BigInteger, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);

        KeyHashGenerator<BigInteger, BigInteger> keyHashGenerator = new SampleBigIntegerKeyHashGenerator(NodeSettings.Default.IDENTIFIER_SIZE);

        // Bootstrap Node
        DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> bootstrapNode = new DHTKademliaNode<>(
                BigInteger.valueOf(0),
                new EmptyConnectionInfo(),
                routingTableFactory.getRoutingTable(BigInteger.valueOf(0)),
                messageSenderAPI,
                nodeSettings,
                new SampleBigIntegerRepository(),
                keyHashGenerator
        );
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        // Other nodes
        for(int i = 1; i < 30; i++){
            BigInteger id = BigInteger.valueOf((int) ((Math.random() * (Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE) - 1)) + 1));
            DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> nextNode = new DHTKademliaNode<>(
                    id,
                    new EmptyConnectionInfo(),
                    routingTableFactory.getRoutingTable(id),
                    messageSenderAPI,
                    nodeSettings,
                    new SampleBigIntegerRepository(),
                    keyHashGenerator
            );
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

    private void testStore(DHTKademliaNodeAPI<BigInteger, EmptyConnectionInfo, BigInteger, String> node, String data) throws ExecutionException, InterruptedException, DuplicateStoreRequest {
        Future<StoreAnswer<BigInteger, BigInteger>> storeFuture = node.store(BigInteger.valueOf(data.hashCode()), data);
        StoreAnswer<BigInteger, BigInteger> storeAnswer = storeFuture.get();
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult() + ", stored in node" + storeAnswer.getNodeId());
        System.out.println(storeAnswer.getNodeId() + " stored " + storeAnswer.getKey());

        if (!storeAnswer.getNodeId().equals(node.getId()))
            Assertions.assertFalse(node.getKademliaRepository().contains(BigInteger.valueOf(data.hashCode())));

        Future<LookupAnswer<BigInteger, BigInteger, String>> lookupFuture = node.lookup(BigInteger.valueOf(data.hashCode()));
        LookupAnswer<BigInteger, BigInteger, String> lookupAnswer = lookupFuture.get();

        Assertions.assertEquals(lookupAnswer.getResult(), LookupAnswer.Result.FOUND);
        Assertions.assertEquals(lookupAnswer.getValue(), data);
        Assertions.assertEquals(lookupAnswer.getNodeId(), storeAnswer.getNodeId());
        System.out.println(lookupAnswer.getNodeId() + " returned the data");
    }

}
