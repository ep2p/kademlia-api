package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.BoundedHashUtil;

public class RedistributionKademliaNodeListener<C extends ConnectionInfo, K, V> implements KademliaNodeListener<C, K, V> {
    private final BoundedHashUtil boundedHashUtil = new BoundedHashUtil(Common.IDENTIFIER_SIZE);
    private final boolean distributeOnShutdown;
    private final ShutdownDistributionListener<C> shutdownDistributionListener;


    public RedistributionKademliaNodeListener(boolean distributeOnShutdown, ShutdownDistributionListener<C> shutdownDistributionListener) {
        this.distributeOnShutdown = distributeOnShutdown;
        this.shutdownDistributionListener = shutdownDistributionListener;
    }

    public RedistributionKademliaNodeListener() {
        this.distributeOnShutdown = false;
        this.shutdownDistributionListener = null;
    }

    @Override
    public void onNewNodeAvailable(KademliaNode<C> kademliaNode, Node<C> node) {
        KademliaRepositoryNode<C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<C, K, V>) kademliaNode;
        RoutingTable<C> routingTable = kademliaRepositoryNode.getRoutingTable();
        kademliaRepositoryNode.getKademliaRepository().getKeys().forEach(key -> {
            FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(boundedHashUtil.hash(((Object) key).hashCode()));
            if (findNodeAnswer.getNodes().size() > 0 && findNodeAnswer.getNodes().get(0).getId() != kademliaRepositoryNode.getId() && findNodeAnswer.getNodes().get(0).getId() == node.getId()) {
                kademliaRepositoryNode.getNodeConnectionApi().storeAsync(kademliaRepositoryNode, kademliaRepositoryNode, node, key, kademliaRepositoryNode.getKademliaRepository().get(key));
            }
        });
    }

    @Override
    public void onKeyStoredResult(KademliaNode<C> kademliaNode, Node<C> node, K key, boolean success) {
        if (node.getId() != kademliaNode.getId()) {
            KademliaRepositoryNode<C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<C, K, V>) kademliaNode;
            kademliaRepositoryNode.getKademliaRepository().remove(key);
        }
    }

    @Override
    public void onBeforeShutdown(KademliaNode<C> kademliaNode) {
        if(distributeOnShutdown){
            KademliaRepositoryNode<C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<C, K, V>) kademliaNode;
            RoutingTable<C> routingTable = kademliaRepositoryNode.getRoutingTable();
            kademliaRepositoryNode.getKademliaRepository().getKeys().forEach(key -> {
                for (Node<C> node : routingTable.findClosest(boundedHashUtil.hash(((Object) key).hashCode())).getNodes()) {
                    if(node.getId() != kademliaRepositoryNode.getId()){
                        try {
                            System.out.println("Closest node to store " + key + " on shutdown is: "+node.getId());
                            kademliaRepositoryNode.getNodeConnectionApi().storeAsync(kademliaNode, kademliaNode, node, key, kademliaRepositoryNode.getKademliaRepository().get(key));
                            break;
                        }catch (Exception ignored){}
                    }
                }
            });
            if(shutdownDistributionListener != null)
                shutdownDistributionListener.onFinish(kademliaRepositoryNode);
        }
    }

    public interface ShutdownDistributionListener<C extends ConnectionInfo> {
        void onFinish(KademliaNode<C> kademliaNode);
    }
}
