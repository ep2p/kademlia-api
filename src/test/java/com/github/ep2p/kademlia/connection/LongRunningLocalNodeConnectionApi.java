package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.node.KademliaNode;
import com.github.ep2p.kademlia.node.KademliaRepositoryNode;
import com.github.ep2p.kademlia.node.Node;

public class LongRunningLocalNodeConnectionApi extends LocalNodeConnectionApi {

    @Override
    public <K, V> void storeAsync(Node<EmptyConnectionInfo> caller, Node<EmptyConnectionInfo> requester, Node<EmptyConnectionInfo> node, K key, V value) {
        System.out.println("storeAsync("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+", "+value+")");
        KademliaNode<EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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

}
