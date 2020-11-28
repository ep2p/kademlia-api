package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.exception.NodeIsOfflineException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaRepositoryNode;
import com.github.ep2p.kademlia.node.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalNodeConnectionApi implements NodeConnectionApi<Integer, EmptyConnectionInfo> {
    protected final Map<Integer, KademliaNode<Integer, EmptyConnectionInfo>> nodeMap = new ConcurrentHashMap<>();

    public void registerNode(KademliaNode<Integer, EmptyConnectionInfo> node){
        System.out.println("Registring node with id " + node.getId());
        nodeMap.putIfAbsent(node.getId(), node);
    }

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LocalNodeConnectionApi() {
        synchronized (nodeMap){
            nodeMap.clear();
        }
    }

    @Override
    public PingAnswer<Integer> ping(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> node) {
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
    public void shutdownSignal(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> node) {
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode != null){
            kademliaNode.onShutdownSignal(caller);
        }
    }

    @Override
    public FindNodeAnswer<Integer, EmptyConnectionInfo> findNode(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> node, Integer destination) {
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            FindNodeAnswer<Integer, EmptyConnectionInfo> findNodeAnswer = new FindNodeAnswer<>(0);
            findNodeAnswer.setAlive(false);
            return findNodeAnswer;
        }
        try {
            return kademliaNode.onFindNode(caller, destination == null ? caller.getId() : destination);
        } catch (NodeIsOfflineException e) {
            return new FindNodeAnswer<>(0);
        }
    }

    @Override
    public <K, V> void storeAsync(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> requester, Node<Integer, EmptyConnectionInfo> node, K key, V value) {
        System.out.println("storeAsync("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+", "+value+")");
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
    public <K> void getRequest(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> requester, Node<Integer, EmptyConnectionInfo> node, K key) {
        System.out.println("getRequest("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+")");
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
    public <K, V> void sendGetResults(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> requester, K key, V value) {
        System.out.println("sendGetResults("+caller.getId()+", "+requester.getId()+", "+key+", "+value+")");
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onGetResult(caller, key, value);
        }
    }

    @Override
    public <K> void sendStoreResults(Node<Integer, EmptyConnectionInfo> caller, Node<Integer, EmptyConnectionInfo> requester, K key, boolean success) {
        System.out.println("sendStoreResults("+caller.getId()+", "+requester.getId()+", "+key+", "+success+")");
        KademliaNode<Integer, EmptyConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onStoreResult(caller, key, success);
        }
    }


}
