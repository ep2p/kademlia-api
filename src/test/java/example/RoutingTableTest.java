package example;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.EmptyConnectionInfo;
import io.ep2p.kademlia.connection.LocalNodeConnectionApi;
import io.ep2p.kademlia.node.IncrementalNodeIdFactory;
import io.ep2p.kademlia.node.KademliaSyncRepositoryNode;
import io.ep2p.kademlia.node.NodeIdFactory;
import io.ep2p.kademlia.node.SampleRepository;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTable;

public class RoutingTableTest {
    public static void main(String[] args) {
        LocalNodeConnectionApi nodeApi = new LocalNodeConnectionApi();
        NodeIdFactory nodeIdFactory = new IncrementalNodeIdFactory();
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.REFERENCED_NODES_UPDATE_PERIOD = 2;
        RoutingTable<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTable =
                new DefaultRoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>>().getRoutingTable(0);

        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node0 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node1 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node2 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node3 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node4 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node5 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node6 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node7 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node8 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());
        KademliaSyncRepositoryNode<Integer, EmptyConnectionInfo, Integer, String> node9 = new KademliaSyncRepositoryNode<>(nodeIdFactory.getNodeId(), nodeApi, new EmptyConnectionInfo(), new SampleRepository());



        routingTable.update(node0);
        routingTable.update(node1);
        routingTable.update(node2);
        routingTable.update(node3);
        routingTable.update(node4);
        routingTable.update(node5);
        routingTable.update(node6);
        routingTable.update(node7);
        routingTable.update(node8);
        routingTable.update(node9);

        routingTable.getBuckets().forEach(bucket -> {
            System.out.println(bucket.getNodeIds());
        });
    }
}
