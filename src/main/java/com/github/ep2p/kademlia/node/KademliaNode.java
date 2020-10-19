package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import lombok.Getter;

import java.util.List;

@Getter
public class KademliaNode<C extends ConnectionInfo> {
    private final NodeApi<C> nodeApi;
    private final Node<C> selfNode;
    private final RoutingTable<C> routingTable;

    public KademliaNode(NodeApi<C> nodeApi, NodeIdFactory nodeIdFactory, C connectionInfo, RoutingTableFactory routingTableFactory) {
        this.nodeApi = nodeApi;
        Integer nodeId = nodeIdFactory.getNodeId();
        selfNode = new Node<>(nodeId, connectionInfo);
        routingTable = routingTableFactory.getRoutingTable(nodeId);
    }

    //first we have to use another node to join network
    public void bootstrap(Node<C> bootstrapNode) throws BootstrapException {
        routingTable.update(this.selfNode);
        //find closest nodes from bootstrap node
        FindNodeAnswer<C> findNodeAnswer = nodeApi.findNode(bootstrapNode, this.selfNode.getId());
        if(!findNodeAnswer.isAlive())
            throw new BootstrapException(bootstrapNode);
        //Ping each node from result and add it to table if alive
        pingAndAddResults(findNodeAnswer.getNodes());
        //Get other closest nodes from alive nodes
        getClosestNodesFromAliveNodes(bootstrapNode);
    }

    private void getClosestNodesFromAliveNodes(Node<C> bootstrapNode) {
        int bucketId = routingTable.findBucket(bootstrapNode.getId()).getId();
        for (int i = 0; ((bucketId - i) > 0 ||
                (bucketId + i) <= Common.IDENTIFIER_SIZE) && i < Common.JOIN_BUCKETS_QUERIES; i++) {
            if (bucketId - i > 0) {
                int idInBucket = routingTable.getIdInPrefix(this.selfNode.getId(),bucketId - i);
                this.findNode(idInBucket);
            }
            if (bucketId + i <= Common.IDENTIFIER_SIZE) {
                int idInBucket = routingTable.getIdInPrefix(this.selfNode.getId(),bucketId + i);
                this.findNode(idInBucket);
            }
        }
    }

    private void findNode(int destination) {
        FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(destination);
        int i = sendFindNodeToBest(findNodeAnswer);
    }

    /** Sends a "FIND_NODE" request to the best "alpha" nodes in a node list */
    public int sendFindNodeToBest(FindNodeAnswer<C> findNodeAnswer) {
        int destination = findNodeAnswer.getDestinationId();
        int i;
        for (i = 0; i < Common.ALPHA && i < findNodeAnswer.size(); i++) {
            ExternalNode<C> externalNode = findNodeAnswer.getNodes().get(i);
            if (externalNode.getNode().getId() != this.selfNode.getId()) {
                FindNodeAnswer<C> findNodeAnswer1 = nodeApi.findNode(externalNode.getNode(), destination);
                if(findNodeAnswer1.getDestinationId() == destination && findNodeAnswer1.isAlive()){
                    routingTable.update(externalNode.getNode());
                    pingAndAddResults(findNodeAnswer.getNodes());
                }
            }
        }
        return i;
    }

    private void pingAndAddResults(List<ExternalNode<C>> cExternalNodes){
        cExternalNodes.forEach(cExternalNode -> {
            PingAnswer pingAnswer = nodeApi.ping(cExternalNode.getNode());
            if(!pingAnswer.isAlive()){
                routingTable.update(cExternalNode.getNode());
            }
        });
    }



}
