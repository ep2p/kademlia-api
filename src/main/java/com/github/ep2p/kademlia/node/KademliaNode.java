package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeApi;
import com.github.ep2p.kademlia.connection.ResponseListener;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import lombok.Getter;

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

    public void bootstrap(Node<C> bootstrapNode) throws BootstrapException {
        routingTable.update(this.selfNode);
        //find closest nodes from bootstrap node
        nodeApi.findNode(bootstrapNode, new ResponseListener<FindNodeAnswer<C>>() {
            @Override
            public void onResponse(FindNodeAnswer<C> findNodeAnswer) {
                //Bootstrap node is alive, lets add it to our table
                routingTable.update(bootstrapNode);
                //ping all close nodes we got from bootstrap node
                findNodeAnswer.getNodes().forEach(cExternalNode -> {
                    nodeApi.ping(cExternalNode.getNode(), new ResponseListener<Void>() {
                        //if ping had response, we add that node to routing table
                        @Override
                        public void onResponse(Void response) {
                            routingTable.update(cExternalNode.getNode());
                        }
                    });
                });
            }

            //if we fail to call bootstrap node, we cant continue
            @Override
            public void onError() {
                throw new RuntimeException(new BootstrapException(bootstrapNode));
            }
        });
    }
}
