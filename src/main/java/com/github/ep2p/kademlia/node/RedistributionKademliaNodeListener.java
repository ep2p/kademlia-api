package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.BoundedHashUtil;

import java.util.Date;

import static com.github.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;

public class RedistributionKademliaNodeListener<C extends ConnectionInfo, K, V> implements KademliaNodeListener<C, K, V> {
    private final BoundedHashUtil boundedHashUtil = new BoundedHashUtil(Common.IDENTIFIER_SIZE);
    private final boolean distributeOnShutdown;
    private final ShutdownDistributionListener<C> shutdownDistributionListener;


    public RedistributionKademliaNodeListener(ShutdownDistributionListener<C> shutdownDistributionListener) {
        this.distributeOnShutdown = true;
        assert shutdownDistributionListener != null;
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
            assert key instanceof Integer;
            FindNodeAnswer<C> findNodeAnswer = routingTable.findClosest(boundedHashUtil.hash((Integer) key));
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
    public void onShutdownComplete(KademliaNode<C> kademliaNode) {
        if(distributeOnShutdown){
            KademliaRepositoryNode<C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<C, K, V>) kademliaNode;
            RoutingTable<C> routingTable = kademliaRepositoryNode.getRoutingTable();
            kademliaRepositoryNode.getKademliaRepository().getKeys().forEach(key -> {
                assert key instanceof Integer;
                Date date = getDateOfSecondsAgo(Common.LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE);
                for (Node<C> node : kademliaNode.getReferencedNodes()) {
                    if(node.getLastSeen().before(date) && kademliaRepositoryNode.getReferencedNodes().contains(node)){
                        kademliaRepositoryNode.getNodeConnectionApi().storeAsync(kademliaNode, kademliaNode, node, key, kademliaRepositoryNode.getKademliaRepository().get(key));
                        break;
                    }
                }
            });
            shutdownDistributionListener.onFinish(kademliaRepositoryNode);
        }
    }

    public interface ShutdownDistributionListener<C extends ConnectionInfo> {
        void onFinish(KademliaNode<C> kademliaNode);
    }
}
