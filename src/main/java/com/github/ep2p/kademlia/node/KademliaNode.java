package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeApi;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.exception.BootstrapException;
import com.github.ep2p.kademlia.exception.NodeIsOfflineException;
import com.github.ep2p.kademlia.exception.ShutdownException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.external.ExternalNode;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.KadDistanceUtil;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static com.github.ep2p.kademlia.Common.BOOTSTRAP_NODE_CALL_TIMEOUT_SEC;
import static com.github.ep2p.kademlia.Common.REFERENCED_NODES_UPDATE_PERIOD_SEC;

public class KademliaNode<ID extends Number, C extends ConnectionInfo> extends Node<ID, C> implements NodeApi<ID, C> {
    //Accessible fields
    @Getter
    private final NodeConnectionApi<ID, C> nodeConnectionApi;
    @Getter
    private final RoutingTable<ID, C, Bucket<ID, C>> routingTable;
    @Getter
    private List<Node<ID, C>> referencedNodes;
    @Getter
    private KademliaNodeListener<ID, C, ?, ?> kademliaNodeListener = new KademliaNodeListener.Default<ID, C>();
    private volatile boolean running;

    //None-Accessible fields
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public KademliaNode(ID nodeId, RoutingTable<ID, C, Bucket<ID, C>> routingTable, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo) {
        this.setId(nodeId);
        this.setConnectionInfo(connectionInfo);
        this.nodeConnectionApi = nodeConnectionApi;
        this.routingTable = routingTable;
        referencedNodes = new CopyOnWriteArrayList<>();
    }

    /**
     * @param bootstrapNode Node to contact to use for bootstrapping current node
     * @throws BootstrapException thrown when bootstrap gets timeout or bootstrap node is not available
     */
    //first we have to use another node to join network
    public void bootstrap(Node<ID, C> bootstrapNode) throws BootstrapException {
        //find closest nodes from bootstrap node
        ID nodeId = this.getId();
        Node<ID, C> selfNode = this;
        Future<FindNodeAnswer<ID, C>> findNodeAnswerFuture = executorService.submit(new Callable<FindNodeAnswer<ID, C>>() {
            @Override
            public FindNodeAnswer<ID, C> call() throws Exception {
                return nodeConnectionApi.findNode(selfNode, bootstrapNode, nodeId);
            }
        });

        try {
            FindNodeAnswer<ID, C> findNodeAnswer = findNodeAnswerFuture.get(BOOTSTRAP_NODE_CALL_TIMEOUT_SEC, TimeUnit.SECONDS);
            if(!findNodeAnswer.isAlive())
                throw new BootstrapException(bootstrapNode);
            //Add bootstrap node to routing table
            addNode(bootstrapNode);
            //Ping each node from result and add it to table if alive
            pingAndAddResults(findNodeAnswer.getNodes());
            //Get other closest nodes from alive nodes
            getClosestNodesFromAliveNodes(bootstrapNode);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BootstrapException(bootstrapNode, e.getMessage(), e);
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                //Get other closest nodes from alive nodes
                getClosestNodesFromAliveNodes(bootstrapNode);
            }
        });
        kademliaNodeListener.onBootstrapDone(this);
        if(!isRunning())
            start();
    }

    /* P2P API */


    /**
     * A node wants to find closest nodes to itself using this node routingTable. Mostly called at bootstrap
     * @param externalNodeId node id for request
     * @return answer of closest nodes
     */
    @Override
    public FindNodeAnswer<ID, C> onFindNode(Node<ID, C> node, ID externalNodeId) throws NodeIsOfflineException {
        if(!isRunning())
            throw new NodeIsOfflineException();
        addNode(node);
        return routingTable.findClosest(externalNodeId);
    }

    /**
     * A node has pinged current node, it means they are alive
     * @param node caller node
     * @return Answer to ping
     */
    @Override
    public PingAnswer<ID> onPing(Node<ID, C> node) throws NodeIsOfflineException {
        if(!isRunning())
            throw new NodeIsOfflineException();
        addNode(node);
        return new PingAnswer<ID>(getId());
    }


    /**
     * A referenced node has told this node that its leaving network
     * @param node node that is shutting down
     */
    @Override
    public void onShutdownSignal(Node<ID, C> node){
        referencedNodes.remove(node);
        routingTable.delete(node);
    }


    /* Managing API */
    public void stop() throws ShutdownException {
        setRunning(false);
        kademliaNodeListener.onBeforeShutdown(this);
        //Shutdown executors
        ShutdownException shutdownException = null;
        try {
            scheduledExecutorService.shutdown();
            executorService.shutdown();
        }catch (Exception e){
            shutdownException = new ShutdownException(e);
        }

        //Tell referenced nodes that we are leaving network
        try {
            referencedNodes.forEach(node -> {
                if(!node.getId().equals(this.getId()))
                    nodeConnectionApi.shutdownSignal(this, node);
            });
        }catch (Exception e){
            shutdownException = new ShutdownException(e);
        }

        kademliaNodeListener.onShutdownComplete(this);

        if(shutdownException != null)
            throw shutdownException;
    }

    public void start(){
        setRunning(true);
        routingTable.update(Node.copy(this));
        //Find maximum n (where n is identifier size) nodes periodically, Check if they are available, otherwise remove them from routing table
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Find maximum n (where n is identifier size) nodes
                makeReferenceNodes();
                //Check if they are available, otherwise remove them from routing table
                pingAndAddResults(referencedNodes);
            }
        }, 0, REFERENCED_NODES_UPDATE_PERIOD_SEC, TimeUnit.SECONDS);
        kademliaNodeListener.onStartupComplete(this);
    }

    public final void setKademliaNodeListener(KademliaNodeListener<ID, C, ?, ?> kademliaNodeListener) {
        assert kademliaNodeListener != null;
        this.kademliaNodeListener = kademliaNodeListener;
    }

    public boolean isRunning(){
        return running;
    }

    /* Helper methods */

    protected void setRunning(boolean running){
        this.running = running;
    }

    protected void addNode(Node<ID, C> node){
        if (routingTable.update(Node.copy(node))) {
            kademliaNodeListener.onNewNodeAvailable(this, node);
        }
    }

    //Gathers most important nodes to keep connection to (See KademliaNodesToReference)
    protected void makeReferenceNodes(){
        referencedNodes = new CopyOnWriteArrayList<>();
        List<ID> distances = KadDistanceUtil.getNodesWithDistance(getId(), Common.IDENTIFIER_SIZE);
        System.out.println("Distances: " + distances);
        distances.forEach(distance -> {
            FindNodeAnswer<ID, C> findNodeAnswer = routingTable.findClosest(distance);
            System.out.println("Items in distance " + distance + " -> " + findNodeAnswer.getNodes());
            if (findNodeAnswer.getNodes().size() > 0) {
                if(!referencedNodes.contains(findNodeAnswer.getNodes().get(0)))
                    referencedNodes.add(findNodeAnswer.getNodes().get(0));
            }
        });
        kademliaNodeListener.onReferencedNodesUpdate(this, referencedNodes);
    }

    protected void getClosestNodesFromAliveNodes(Node<ID, C> bootstrapNode) {
        int bucketId = routingTable.findBucket(bootstrapNode.getId()).getId();
        final Set<ID> destinations = new LinkedHashSet<>();
        for (int i = 0; ((bucketId - i) > 0 || (bucketId + i) <= Common.IDENTIFIER_SIZE) && i < Common.JOIN_BUCKETS_QUERIES; i++) {
            Number idInBucket = -1;
            if (bucketId - i > 0) {
                idInBucket = routingTable.getIdInPrefix(this.getId(),bucketId - i);
                destinations.add((ID) idInBucket);
            }
            if (bucketId + i <= Common.IDENTIFIER_SIZE) {
                idInBucket = routingTable.getIdInPrefix(this.getId(),bucketId + i);
            }
            if(!idInBucket.equals(-1))
                destinations.add((ID) idInBucket);
        }
        destinations.forEach(this::findBestNodesCloseToDestination);
    }

    protected void findBestNodesCloseToDestination(ID destination) {
        FindNodeAnswer<ID, C> findNodeAnswer = routingTable.findClosest(destination);
        sendFindNodeToBest(findNodeAnswer);
    }

    /** Sends a "FIND_NODE" request to the best "alpha" nodes in a node list */
    protected int sendFindNodeToBest(FindNodeAnswer<ID, C> findNodeAnswer) {
        ID destination = findNodeAnswer.getDestinationId();
        int i;
        for (i = 0; i < Common.ALPHA && i < findNodeAnswer.size(); i++) {
            ExternalNode<ID, C> externalNode = findNodeAnswer.getNodes().get(i);
            if (!externalNode.getId().equals(this.getId())) {
                FindNodeAnswer<ID, C> findNodeAnswer1 = nodeConnectionApi.findNode(this, externalNode, destination);
                if(findNodeAnswer1.getDestinationId().equals(destination) && findNodeAnswer1.isAlive()){
                    addNode(externalNode);
                    pingAndAddResults(findNodeAnswer.getNodes());
                }
            }
        }
        return i;
    }

    protected void pingAndAddResults(List<? extends Node<ID, C>> externalNodes){
        for (Node<ID, C> externalNode : externalNodes) {
            if(externalNode.getId().equals(getId()))
                continue;
            PingAnswer<ID> pingAnswer = nodeConnectionApi.ping(this, externalNode);
            if(pingAnswer.isAlive()){
                addNode(externalNode);
            }else {
                routingTable.delete(externalNode);
            }
        }
    }
}
