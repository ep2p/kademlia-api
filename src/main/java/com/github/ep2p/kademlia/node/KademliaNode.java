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
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.KadDistanceUtil;
import lombok.Getter;

import javax.annotation.PreDestroy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static com.github.ep2p.kademlia.Common.BOOTSTRAP_NODE_CALL_TIMEOUT_SEC;
import static com.github.ep2p.kademlia.Common.REFERENCED_NODES_UPDATE_PERIOD_SEC;

public class KademliaNode<C extends ConnectionInfo> extends Node<C> implements NodeApi<C> {
    //Accessible fields
    @Getter
    private final NodeConnectionApi<C> nodeConnectionApi;
    @Getter
    private final RoutingTable<C> routingTable;
    @Getter
    private List<Node<C>> referencedNodes;
    @Getter
    private KademliaNodeListener<C, ?, ?> kademliaNodeListener = new KademliaNodeListener.Default<C>();
    private volatile boolean running;

    //None-Accessible fields
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public KademliaNode(Integer nodeId, RoutingTable<C> routingTable, NodeConnectionApi<C> nodeConnectionApi, C connectionInfo) {
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
    public void bootstrap(Node<C> bootstrapNode) throws BootstrapException {
        //find closest nodes from bootstrap node
        Integer nodeId = this.getId();
        Node<C> selfNode = this;
        Future<FindNodeAnswer<C>> findNodeAnswerFuture = executorService.submit(new Callable<FindNodeAnswer<C>>() {
            @Override
            public FindNodeAnswer<C> call() throws Exception {
                return nodeConnectionApi.findNode(selfNode, bootstrapNode, nodeId);
            }
        });

        try {
            FindNodeAnswer<C> findNodeAnswer = findNodeAnswerFuture.get(BOOTSTRAP_NODE_CALL_TIMEOUT_SEC, TimeUnit.SECONDS);
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
        start();
    }

    /* P2P API */


    /**
     * A node wants to find closest nodes to itself using this node routingTable. Mostly called at bootstrap
     * @param externalNodeId node id for request
     * @return answer of closest nodes
     */
    @Override
    public FindNodeAnswer<C> onFindNode(int externalNodeId) throws NodeIsOfflineException {
        if(!isRunning())
            throw new NodeIsOfflineException();
        return routingTable.findClosest(externalNodeId);
    }

    /**
     * A node has pinged current node, it means they are alive
     * @param node caller node
     * @return Answer to ping
     */
    @Override
    public PingAnswer onPing(Node<C> node) throws NodeIsOfflineException {
        if(!isRunning())
            throw new NodeIsOfflineException();
        addNode(node);
        return new PingAnswer(getId());
    }


    /**
     * A referenced node has told this node that its leaving network
     * @param node node that is shutting down
     */
    @Override
    public void onShutdownSignal(Node<C> node){
        referencedNodes.remove(node);
        routingTable.delete(node);
    }


    /* Managing API */
    @PreDestroy
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
                if(node.getId() != this.getId())
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
        routingTable.update(this);
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

    public final void setKademliaNodeListener(KademliaNodeListener kademliaNodeListener) {
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

    protected void addNode(Node<C> node){
        if (routingTable.update(node)) {
            kademliaNodeListener.onNewNodeAvailable(this, node);
        }
    }

    //Gathers most important nodes to keep connection to (See KademliaNodesToReference)
    protected void makeReferenceNodes(){
        referencedNodes = new CopyOnWriteArrayList<>();
        List<Integer> distances = KadDistanceUtil.getNodesWithDistance(getId(), Common.IDENTIFIER_SIZE);
        distances.forEach(distance -> {
            FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(distance);
            if (findNodeAnswer.getNodes().size() > 0) {
                if(!referencedNodes.contains(findNodeAnswer.getNodes().get(0)))
                    referencedNodes.add(findNodeAnswer.getNodes().get(0));
            }
        });
        kademliaNodeListener.onReferencedNodesUpdate(this, referencedNodes);
    }

    protected void getClosestNodesFromAliveNodes(Node<C> bootstrapNode) {
        int bucketId = routingTable.findBucket(bootstrapNode.getId()).getId();
        final Set<Integer> destinations = new LinkedHashSet<>();
        for (int i = 0; ((bucketId - i) > 0 || (bucketId + i) <= Common.IDENTIFIER_SIZE) && i < Common.JOIN_BUCKETS_QUERIES; i++) {
            int idInBucket = -1;
            if (bucketId - i > 0) {
                idInBucket = routingTable.getIdInPrefix(this.getId(),bucketId - i);
                destinations.add(idInBucket);
            }
            if (bucketId + i <= Common.IDENTIFIER_SIZE) {
                idInBucket = routingTable.getIdInPrefix(this.getId(),bucketId + i);
            }
            if(idInBucket != -1)
                destinations.add(idInBucket);
        }
        destinations.forEach(this::findBestNodesCloseToDestination);
    }

    protected void findBestNodesCloseToDestination(int destination) {
        FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(destination);
        sendFindNodeToBest(findNodeAnswer);
    }

    /** Sends a "FIND_NODE" request to the best "alpha" nodes in a node list */
    protected int sendFindNodeToBest(FindNodeAnswer<C> findNodeAnswer) {
        int destination = findNodeAnswer.getDestinationId();
        int i;
        for (i = 0; i < Common.ALPHA && i < findNodeAnswer.size(); i++) {
            ExternalNode<C> externalNode = findNodeAnswer.getNodes().get(i);
            if (externalNode.getId() != this.getId()) {
                FindNodeAnswer<C> findNodeAnswer1 = nodeConnectionApi.findNode(this, externalNode, destination);
                if(findNodeAnswer1.getDestinationId() == destination && findNodeAnswer1.isAlive()){
                    addNode(externalNode);
                    pingAndAddResults(findNodeAnswer.getNodes());
                }
            }
        }
        return i;
    }

    protected void pingAndAddResults(List<? extends Node<C>> externalNodes){
        for (Node<C> externalNode : externalNodes) {
            if(externalNode.getId() == getId())
                continue;
            PingAnswer pingAnswer = nodeConnectionApi.ping(this, externalNode);
            if(pingAnswer.isAlive()){
                addNode(externalNode);
            }else {
                routingTable.delete(externalNode);
            }
        }
    }
}
