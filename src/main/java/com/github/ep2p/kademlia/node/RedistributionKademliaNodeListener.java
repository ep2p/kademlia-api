package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.LongRoutingTable;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.BoundedHashUtil;

public class RedistributionKademliaNodeListener<ID extends Number, C extends ConnectionInfo, K, V> implements KademliaNodeListener<ID, C, K, V> {
    private final BoundedHashUtil boundedHashUtil = new BoundedHashUtil(Common.IDENTIFIER_SIZE);
    private final boolean distributeOnShutdown;
    private final ShutdownDistributionListener<ID, C> shutdownDistributionListener;


    public RedistributionKademliaNodeListener(boolean distributeOnShutdown, ShutdownDistributionListener<ID, C> shutdownDistributionListener) {
        this.distributeOnShutdown = distributeOnShutdown;
        this.shutdownDistributionListener = shutdownDistributionListener;
    }

    public RedistributionKademliaNodeListener() {
        this.distributeOnShutdown = false;
        this.shutdownDistributionListener = null;
    }

    @Override
    public void onNewNodeAvailable(KademliaNode<ID, C> kademliaNode, Node<ID, C> node) {
        KademliaRepositoryNode<ID, C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<ID, C, K, V>) kademliaNode;
        RoutingTable<ID, C, Bucket<ID, C>> routingTable = kademliaRepositoryNode.getRoutingTable();
        kademliaRepositoryNode.getKademliaRepository().getKeys().forEach(key -> {
            FindNodeAnswer<ID, C> findNodeAnswer = routingTable.findClosest(hash(kademliaNode.getId(), key));
            if (findNodeAnswer.getNodes().size() > 0 && !findNodeAnswer.getNodes().get(0).getId().equals(kademliaRepositoryNode.getId()) && findNodeAnswer.getNodes().get(0).getId().equals(node.getId())) {
                kademliaRepositoryNode.getNodeConnectionApi().storeAsync(kademliaRepositoryNode, kademliaRepositoryNode, node, key, kademliaRepositoryNode.getKademliaRepository().get(key));
            }
        });
    }

    @Override
    public void onKeyStoredResult(KademliaNode<ID, C> kademliaNode, Node<ID, C> node, K key, boolean success) {
        if (!node.getId().equals(kademliaNode.getId())) {
            KademliaRepositoryNode<ID, C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<ID, C, K, V>) kademliaNode;
            kademliaRepositoryNode.getKademliaRepository().remove(key);
        }
    }

    @Override
    public void onBeforeShutdown(KademliaNode<ID, C> kademliaNode) {
        if(distributeOnShutdown){
            KademliaRepositoryNode<ID, C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<ID, C, K, V>) kademliaNode;
            RoutingTable<ID, C, Bucket<ID, C>> routingTable = kademliaRepositoryNode.getRoutingTable();
            kademliaRepositoryNode.getKademliaRepository().getKeys().forEach(key -> {
                for (Node<ID, C> node : routingTable.findClosest(hash(kademliaNode.getId(), key)).getNodes()) {
                    if(!node.getId().equals(kademliaRepositoryNode.getId())){
                        try {
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

    protected ID hash(ID nodeId, K key){
        if (key.getClass().equals(nodeId.getClass())) {
            return (ID) key;
        }else {
            return boundedHashUtil.hash(key.hashCode(), (Class<ID>) nodeId.getClass());
        }
    }

    public interface ShutdownDistributionListener<ID extends Number, C extends ConnectionInfo> {
        void onFinish(KademliaNode<ID, C> kademliaNode);
    }
}
