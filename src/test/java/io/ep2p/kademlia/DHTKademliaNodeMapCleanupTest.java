package io.ep2p.kademlia;

import io.ep2p.kademlia.helpers.EmptyConnectionInfo;
import io.ep2p.kademlia.helpers.SampleKeyHashGenerator;
import io.ep2p.kademlia.helpers.SampleRepository;
import io.ep2p.kademlia.helpers.TestMessageSenderAPI;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNode;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTableFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class DHTKademliaNodeMapCleanupTest {

    // Testing if store and lookup maps is getting cleaned after we store the data

    @SneakyThrows
    @Test
    void testCleanStoreAndLookupMaps() {
        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings nodeSettings = NodeSettings.Default.build();

        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);

        KeyHashGenerator<Integer, Integer> keyHashGenerator = new SampleKeyHashGenerator(NodeSettings.Default.IDENTIFIER_SIZE);

        // Bootstrap Node
        DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> bootstrapNode = new DHTKademliaNode<>(0, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(0), messageSenderAPI, nodeSettings, new SampleRepository(), keyHashGenerator);
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        // Other nodes
        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> nextNode = new DHTKademliaNode<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, nodeSettings, new SampleRepository(), keyHashGenerator);
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
        }

        String data = "Eleuth";
        StoreAnswer<Integer, Integer> storeAnswer = bootstrapNode.store(data.hashCode(), data).get();

        Thread.sleep(100); // giving time for cleanup

        Field field = bootstrapNode.getClass().getDeclaredField("storeService");
        field.setAccessible(true);
        Object storeService = field.get(bootstrapNode);

        field = storeService.getClass().getDeclaredField("storeMap");
        field.setAccessible(true);
        Object storeMap = field.get(storeService);
        Assertions.assertFalse(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).containsKey(data.hashCode()));
        Assertions.assertEquals(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).size(), 0);

        boolean exceptionThrown = false;
        try {
            data = UUID.randomUUID().toString();
            storeAnswer = bootstrapNode.store(data.hashCode(), data).get(1, TimeUnit.NANOSECONDS);
            System.out.println("stored second one! :O ");
        } catch (Exception e){
            exceptionThrown = true;
            System.out.println("This exception has to happen");
            e.printStackTrace();
        } finally {
            System.out.println(storeAnswer);
        }

        Assertions.assertTrue(exceptionThrown);

        Thread.sleep(100); // giving time for cleanup
        Assertions.assertFalse(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).containsKey(data.hashCode()));
        Assertions.assertEquals(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).size(), 0);

        messageSenderAPI.stopAll();

    }

}
