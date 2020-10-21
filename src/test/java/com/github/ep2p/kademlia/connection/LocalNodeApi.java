package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.Node;

import java.util.HashMap;
import java.util.Map;

public class LocalNodeApi implements NodeApi<EmptyConnectionInfo>{
    private final static Map<Integer, KademliaNode<EmptyConnectionInfo>> nodeMap = new HashMap<>();


    public static void registerNode(KademliaNode<EmptyConnectionInfo> node){
        nodeMap.putIfAbsent(node.getId(), node);
    }

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
    public <K, V, R> R store(Node<EmptyConnectionInfo> node, K key, V value) {
        return null;
    }

    @Override
    public <K, R> R get(Node<EmptyConnectionInfo> node, K key) {
        return null;
    }
}
