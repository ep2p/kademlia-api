package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaRepositoryNode;
import com.github.ep2p.kademlia.node.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalNodeApi implements NodeApi<EmptyConnectionInfo>{
    private final static Map<Integer, KademliaNode<EmptyConnectionInfo>> nodeMap = new HashMap<>();

    public static <E extends KademliaNode<EmptyConnectionInfo>> void registerNode(E node){
        System.out.println("Registring node with id " + node.getId());
        nodeMap.putIfAbsent(node.getId(), node);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public PingAnswer ping(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> node) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            PingAnswer pingAnswer = new PingAnswer(node.getId());
            pingAnswer.setAlive(false);
            return pingAnswer;
        }
        return kademliaNode.onPing(caller);
    }

    @Override
    public void shutdownSignal(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> node) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode != null){
            kademliaNode.onShutdownSignal(caller);
        }
    }

    @Override
    public FindNodeAnswer<EmptyConnectionInfo> findNode(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> node, Integer destination) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode == null){
            FindNodeAnswer<EmptyConnectionInfo> findNodeAnswer = new FindNodeAnswer<>(0);
            findNodeAnswer.setAlive(false);
            return findNodeAnswer;
        }
        return kademliaNode.onFindNode(destination == null ? caller.getId() : destination);
    }

    @Override
    public <K, V> void storeAsync(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> requester, Node<EmptyConnectionInfo> node, K key, V value) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    ((KademliaRepositoryNode) kademliaNode).onStoreRequest(requester, caller, key, value);
                }
            });
        }
        if(kademliaNode == null){
            throw new RuntimeException("Node "+ node.getId() +" not available");
        }
    }

    @Override
    public <K> void getRequest(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> requester, Node<EmptyConnectionInfo> node, K key) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onGetRequest(caller, requester, key);
        }
    }


    @Override
    public <K, V> void sendGetResults(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> requester, K key, V value) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onGetResult(caller, key, value);
        }
    }

    @Override
    public <K> void sendStoreResults(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> requester, K key, boolean success) {
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(requester.getId());
        if(kademliaNode instanceof KademliaRepositoryNode){
            ((KademliaRepositoryNode) kademliaNode).onStoreResult(caller, key, success);
        }
    }


}
