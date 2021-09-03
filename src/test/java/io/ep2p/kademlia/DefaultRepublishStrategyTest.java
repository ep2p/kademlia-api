package io.ep2p.kademlia;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class DefaultRepublishStrategyTest {

    @Test
    public void testRepublish() throws BootstrapException, StoreException, InterruptedException, GetException {
        LocalNodeConnectionApi<Integer> nodeApi = new LocalNodeConnectionApi<>();

        int REPUBLISH_INTERVAL_SEC_CONSTANT = 3;

        NodeSettings.RepublishSettings republishSettings = NodeSettings.RepublishSettings.builder()
                .republishIntervalUnit(TimeUnit.SECONDS)
                .republishIntervalValue(REPUBLISH_INTERVAL_SEC_CONSTANT)
                .republishQuerySize(100)
                .build();

        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;
        NodeSettings.Default.ENABLED_KEY_REPUBLISHING = true;
        NodeSettings.Default.REPUBLISH_SETTINGS = republishSettings;



        //bootstrap node
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new TimestampAwareRepositoryStub());
        nodeApi.registerNode(node0);
        node0.start();

        Thread.sleep(100);

        for(int i = 1; i < Math.pow(2, NodeSettings.Default.IDENTIFIER_SIZE); i++){
            // lets not make node 0 now
            if (i == 11){
                continue;
            }
            KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> aNode = new KademliaRepositoryNode<>(i, nodeApi, new EmptyConnectionInfo(), new TimestampAwareRepositoryStub());
            Thread.sleep(100);
            nodeApi.registerNode(aNode);
            aNode.bootstrap(node0);
        }

        // give them all some time to fully initialize
        Thread.sleep(2000);

        String data = "Eleuth"; // when 11 is available it should go to 11, otherwise goes to 10!
        StoreAnswer<Integer, Integer> storeAnswer = node0.store(data.hashCode(), data, true);
        Assertions.assertEquals(storeAnswer.getResult(), StoreAnswer.Result.STORED, "StoreAnswer Result was " + storeAnswer.getResult());
        Assertions.assertNotNull(node0.getRepublishStrategy());

        KademliaRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node11 = new KademliaRepositoryNode<>(11, nodeApi, new EmptyConnectionInfo(), new TimestampAwareRepositoryStub());
        Thread.sleep(100);
        nodeApi.registerNode(node11);
        node11.bootstrap(node0);

        Thread.sleep(REPUBLISH_INTERVAL_SEC_CONSTANT * 2 * 1100);

        Assertions.assertTrue(node11.getKademliaRepository().contains(data.hashCode()));


    }

}
