package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaRepositoryNode;
import com.github.ep2p.kademlia.node.Node;

public class LongRunningLocalNodeConnectionApi extends LocalNodeConnectionApi {

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
                        Thread.sleep(2000);
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
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((KademliaRepositoryNode) kademliaNode).onGetRequest(caller, requester, key);
                }
            });
        }
    }

}
