package io.ep2p.kademlia;

import io.ep2p.kademlia.helpers.EmptyConnectionInfo;
import io.ep2p.kademlia.helpers.SampleKeyHashGenerator;
import io.ep2p.kademlia.helpers.SampleRepository;
import io.ep2p.kademlia.helpers.TestMessageSenderAPI;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.node.builder.DHTKademliaNodeBuilder;
import io.ep2p.kademlia.services.DHTStoreServiceFactory;
import io.ep2p.kademlia.services.PullingDHTStoreService;
import io.ep2p.kademlia.services.PushingDHTStoreService;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DHTPullingTest {

    @Test
    void testStore() throws ExecutionException, InterruptedException {
        DHTStoreServiceFactory<Integer, EmptyConnectionInfo, Integer, String> dhtStoreServiceFactory = new DHTStoreServiceFactory<Integer, EmptyConnectionInfo, Integer, String>() {
            @Override
            public PushingDHTStoreService<Integer, EmptyConnectionInfo, Integer, String> getDhtStoreService(DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> kademliaNodeAPI) {
                return new PullingDHTStoreService<>(kademliaNodeAPI, Executors.newFixedThreadPool((int) Math.ceil(kademliaNodeAPI.getNodeSettings().getDhtExecutorPoolSize()/2)));
            }
        };

        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings nodeSettings = NodeSettings.Default.build();

        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);

        KeyHashGenerator<Integer, Integer> keyHashGenerator = new SampleKeyHashGenerator(NodeSettings.Default.IDENTIFIER_SIZE);

        // Bootstrap Node
        DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> bootstrapNode = new DHTKademliaNodeBuilder<>(0, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(0), messageSenderAPI, keyHashGenerator, new SampleRepository<>()).setDhtStoreServiceFactory(dhtStoreServiceFactory).build();
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        // Other nodes
        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> nextNode = new DHTKademliaNodeBuilder<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, keyHashGenerator, new SampleRepository<>()).setDhtStoreServiceFactory(dhtStoreServiceFactory).build();
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
        }


        Thread.sleep(2000);

        String baseKey = "Eleuth";
        testStore(bootstrapNode, baseKey);
        String value = bootstrapNode.getKademliaRepository().get(baseKey.hashCode());
        Assertions.assertNull(value, "Bootstrap node should have removed `" + baseKey + "`");
        for (int i = 0; i < 10; i++){
            testStore(bootstrapNode, UUID.randomUUID().toString());
        }

        messageSenderAPI.stopAll();
    }

    private void testStore(DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> node, String data) throws ExecutionException, InterruptedException {
        Future<StoreAnswer<Integer, EmptyConnectionInfo, Integer>> storeFuture = node.store(data.hashCode(), data);
        StoreAnswer<Integer, EmptyConnectionInfo, Integer> storeAnswer = storeFuture.get();
        Assertions.assertEquals( StoreAnswer.Result.STORED, storeAnswer.getResult(), "StoreAnswer Result was " + storeAnswer.getResult() + ", stored in node" + storeAnswer.getNode().getId());
        System.out.println(storeAnswer.getNode().getId() + " stored " + storeAnswer.getKey());

        if (!storeAnswer.getNode().getId().equals(node.getId()))
            Assertions.assertFalse(node.getKademliaRepository().contains(data.hashCode()));

        Future<LookupAnswer<Integer, EmptyConnectionInfo, Integer, String>> lookupFuture = node.lookup(data.hashCode());
        LookupAnswer<Integer, EmptyConnectionInfo, Integer, String> lookupAnswer = lookupFuture.get();

        Assertions.assertEquals(LookupAnswer.Result.FOUND, lookupAnswer.getResult());
        Assertions.assertEquals(lookupAnswer.getValue(), data);
        Assertions.assertEquals(lookupAnswer.getNode().getId(), storeAnswer.getNode().getId());

        System.out.println(lookupAnswer.getNode().getId() + " returned the data");
    }

}
