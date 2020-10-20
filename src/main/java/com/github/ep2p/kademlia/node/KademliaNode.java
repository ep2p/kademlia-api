package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.KadDistanceUtil;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.exception.ShutdownException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.table.RoutingTableFactory;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.*;

import static com.github.ep2p.kademlia.Common.BOOTSTRAP_NODE_CALL_TIMEOUT_SEC;

public class KademliaNode<C extends ConnectionInfo> extends Node<C> {
    //Accessible fields
    @Getter
    private final NodeApi<C> nodeApi;
    @Getter
    private final RoutingTable<C> routingTable;
    @Getter
    private final List<Node<C>> referenceNodes;

    //None-Accessible fields
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public KademliaNode(NodeApi<C> nodeApi, NodeIdFactory nodeIdFactory, C connectionInfo, RoutingTableFactory routingTableFactory) {
        this.nodeApi = nodeApi;
        Integer nodeId = nodeIdFactory.getNodeId();
        routingTable = routingTableFactory.getRoutingTable(nodeId);
        this.setId(nodeId);
        this.setConnection(connectionInfo);
        referenceNodes = new CopyOnWriteArrayList<>();
    }

    //first we have to use another node to join network
    public void bootstrap(Node<C> bootstrapNode) throws BootstrapException {
        routingTable.update(this);
        //find closest nodes from bootstrap node
        Integer nodeId = this.getId();
        Future<FindNodeAnswer<C>> findNodeAnswerFuture = executorService.submit(new Callable<FindNodeAnswer<C>>() {
            @Override
            public FindNodeAnswer<C> call() throws Exception {
                return nodeApi.findNode(bootstrapNode, nodeId);
            }
        });

        try {
            FindNodeAnswer<C> findNodeAnswer = findNodeAnswerFuture.get(BOOTSTRAP_NODE_CALL_TIMEOUT_SEC, TimeUnit.SECONDS);
            if(!findNodeAnswer.isAlive())
                throw new BootstrapException(bootstrapNode);
            //Add bootstrap node to routing table
            routingTable.update(bootstrapNode);
            //Ping each node from result and add it to table if alive
            pingAndAddResults(findNodeAnswer.getNodes());
            //Get other closest nodes from alive nodes
            getClosestNodesFromAliveNodes(bootstrapNode);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            executorService.shutdown();
            throw new BootstrapException(bootstrapNode, e.getMessage(), e);
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                //Get other closest nodes from alive nodes
                getClosestNodesFromAliveNodes(bootstrapNode);
                executorService.shutdown();
            }
        });
    }

    private void stop() throws ShutdownException {
        ShutdownException shutdownException = null;
        try {
            scheduledExecutorService.shutdown();
            executorService.shutdown();
        }catch (Exception e){
            shutdownException = new ShutdownException(e);
        }

        try {
            referenceNodes.forEach(nodeApi::shutdownSignal);
        }catch (Exception e){
            shutdownException = new ShutdownException(e);
        }

        if(shutdownException != null)
            throw shutdownException;
    }

    private void start(){
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                makeReferenceNodes();
                pingAndAddResults(referenceNodes);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void makeReferenceNodes(){
        List<Integer> distances = KadDistanceUtil.getNodesWithDistance(getId(), Common.IDENTIFIER_SIZE);
        distances.forEach(distance -> {
            FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(distance);
            if (findNodeAnswer.getNodes().size() > 0) {
                if(!referenceNodes.contains(findNodeAnswer.getNodes().get(0)))
                    referenceNodes.add(findNodeAnswer.getNodes().get(0));
            }
        });
    }

    private void getClosestNodesFromAliveNodes(Node<C> bootstrapNode) {
        int bucketId = routingTable.findBucket(bootstrapNode.getId()).getId();
        for (int i = 0; ((bucketId - i) > 0 ||
                (bucketId + i) <= Common.IDENTIFIER_SIZE) && i < Common.JOIN_BUCKETS_QUERIES; i++) {
            if (bucketId - i > 0) {
                int idInBucket = routingTable.getIdInPrefix(this.getId(),bucketId - i);
                this.findNode(idInBucket);
            }
            if (bucketId + i <= Common.IDENTIFIER_SIZE) {
                int idInBucket = routingTable.getIdInPrefix(this.getId(),bucketId + i);
                this.findNode(idInBucket);
            }
        }
    }

    private void findNode(int destination) {
        FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(destination);
        sendFindNodeToBest(findNodeAnswer);
    }

    /** Sends a "FIND_NODE" request to the best "alpha" nodes in a node list */
    public int sendFindNodeToBest(FindNodeAnswer<C> findNodeAnswer) {
        int destination = findNodeAnswer.getDestinationId();
        int i;
        for (i = 0; i < Common.ALPHA && i < findNodeAnswer.size(); i++) {
            ExternalNode<C> externalNode = findNodeAnswer.getNodes().get(i);
            if (externalNode.getId() != this.getId()) {
                FindNodeAnswer<C> findNodeAnswer1 = nodeApi.findNode(externalNode, destination);
                if(findNodeAnswer1.getDestinationId() == destination && findNodeAnswer1.isAlive()){
                    routingTable.update(externalNode);
                    pingAndAddResults(findNodeAnswer.getNodes());
                }
            }
        }
        return i;
    }

    private void pingAndAddResults(List<? extends Node<C>> externalNodes){
        externalNodes.forEach(externalNode -> {
            PingAnswer pingAnswer = nodeApi.ping(externalNode);
            if(pingAnswer.isAlive()){
                routingTable.update(externalNode);
            }else {
                routingTable.delete(externalNode);
            }
        });
    }


}
