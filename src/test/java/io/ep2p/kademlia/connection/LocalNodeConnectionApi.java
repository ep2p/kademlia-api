package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.exception.NodeIsOfflineException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.PingAnswer;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaRepositoryNode;
import io.ep2p.kademlia.node.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalNodeConnectionApi<ID extends Number> implements NodeConnectionApi<ID, EmptyConnectionInfo> {
    protected final Map<ID, KademliaNode<ID, EmptyConnectionInfo>> nodeMap = new ConcurrentHashMap<>();

    public void registerNode(KademliaNode<ID, EmptyConnectionInfo> node){
        System.out.println("Registering node with id " + node.getId());
        nodeMap.putIfAbsent(node.getId(), node);
    }

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LocalNodeConnectionApi() {
        synchronized (nodeMap){
            nodeMap.clear();
        }
    }

    @Override
    public PingAnswer<ID> ping(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> node) {
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            PingAnswer pingAnswer = new PingAnswer(node.getId());
            pingAnswer.setAlive(false);
            return pingAnswer;
        }
        try {
            return kademliaNode.onPing(caller);
        } catch (NodeIsOfflineException e) {
            return new PingAnswer(node.getId(), false);
        }
    }

    @Override
    public void shutdownSignal(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> node) {
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode != null){
            kademliaNode.onShutdownSignal(caller);
        }
    }

    @Override
    public FindNodeAnswer<ID, EmptyConnectionInfo> findNode(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> node, ID destination) {
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            FindNodeAnswer<ID, EmptyConnectionInfo> findNodeAnswer = new FindNodeAnswer(0);
            findNodeAnswer.setAlive(false);
            return findNodeAnswer;
        }
        try {
            return kademliaNode.onFindNode(caller, destination == null ? caller.getId() : destination);
        } catch (NodeIsOfflineException e) {
            return new FindNodeAnswer(0);
        }
    }

    @Override
    public <K, V> void storeAsync(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> requester, Node<ID, EmptyConnectionInfo> node, K key, V value) {
        System.out.println("storeAsync("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+", "+value+")");
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Fake network latency
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((KademliaRepositoryNode) kademliaNode).onStoreRequest(caller, requester, key, value);
                }
            });
        }
        if(kademliaNode == null){
            throw new RuntimeException("Node "+ node.getId() +" not available");
        }
    }

    @Override
    public <K> void getRequest(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> requester, Node<ID, EmptyConnectionInfo> node, K key) {
        System.out.println("getRequest("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+")");
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Fake network latency
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((KademliaRepositoryNode) kademliaNode).onGetRequest(caller, requester, key);
                }
            });
        }
    }


    @Override
    public <K, V> void sendGetResults(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> requester, K key, V value) {
        System.out.println("sendGetResults("+caller.getId()+", "+requester.getId()+", "+key+", "+value+")");
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onGetResult(caller, key, value);
        }
    }

    @Override
    public <K> void sendStoreResults(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> requester, K key, boolean success) {
        System.out.println("sendStoreResults("+caller.getId()+", "+requester.getId()+", "+key+", "+success+")");
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onStoreResult(caller, key, success);
        }
    }


}
