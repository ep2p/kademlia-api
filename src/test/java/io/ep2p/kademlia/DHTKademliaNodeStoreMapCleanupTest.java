package io.ep2p.kademlia;

import io.ep2p.kademlia.helpers.EmptyConnectionInfo;
import io.ep2p.kademlia.helpers.SampleRepository;
import io.ep2p.kademlia.helpers.TestMessageSenderAPI;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DHTKademliaNodeStoreMapCleanupTest {

    // Testing if store map is getting cleaned after we store the data

    @SneakyThrows
    @Test
    public void TestCleanStoreMap() {
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


        // Wait and test if all nodes join
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            while (messageSenderAPI.map.size() < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE)){
                //wait
            }
            countDownLatch.countDown();
        }).start();
        boolean await = countDownLatch.await(NodeSettings.Default.PING_SCHEDULE_TIME_VALUE + 1, NodeSettings.Default.PING_SCHEDULE_TIME_UNIT);
        Assertions.assertTrue(await);

        System.out.println("All nodes tried registry in the right time");

        Thread.sleep(2000);

        String data = "Eleuth";
        StoreAnswer<Integer, Integer> storeAnswer = bootstrapNode.store(data.hashCode(), data).get();

        Thread.sleep(100); // giving time for cleanup

        Field field = bootstrapNode.getClass().getDeclaredField("storeMap");
        field.setAccessible(true);
        Object storeMap = field.get(bootstrapNode);
        Assertions.assertFalse(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).containsKey(data.hashCode()));
        Assertions.assertEquals(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).size(), 0);

        data = UUID.randomUUID().toString();
        storeAnswer = bootstrapNode.store(data.hashCode(), data).get(1, TimeUnit.MICROSECONDS);

        Thread.sleep(100); // giving time for cleanup

        Assertions.assertFalse(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).containsKey(data.hashCode()));
        Assertions.assertEquals(((Map<Integer, StoreAnswer<Integer, Integer>>) storeMap).size(), 0);


    }

}
