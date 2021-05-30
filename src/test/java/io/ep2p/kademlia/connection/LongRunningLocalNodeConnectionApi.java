package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaRepositoryNode;
import io.ep2p.kademlia.node.Node;

public class LongRunningLocalNodeConnectionApi<ID extends Number> extends LocalNodeConnectionApi<ID> {

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
    public <K> void getRequest(Node<ID, EmptyConnectionInfo> caller, Node<ID, EmptyConnectionInfo> requester, Node<ID, EmptyConnectionInfo> node, K key) {
        System.out.println("getRequest("+caller.getId()+", "+requester.getId()+", "+node.getId()+", "+key+")");
        KademliaNode<ID, EmptyConnectionInfo> kademliaNode = nodeMap.get(node.getId());
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
