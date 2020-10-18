package com.github.ep2p.kademlia.node;

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
        FindNodeAnswer<C> findNodeAnswer = nodeApi.findNode(bootstrapNode);
        //Ping each node from result and add it to table if alive
        pingAndAddResults(findNodeAnswer.getNodes());

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
