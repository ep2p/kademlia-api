package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.BoundedHashUtil;

public class RedistributionKademliaNodeListener<C extends ConnectionInfo, K, V> implements KademliaNodeListener<C, K, V> {
    @Override
    public void onNewNodeAvailable(KademliaNode<C> kademliaNode, Node<C> node) {
        KademliaRepositoryNode<C, K, V> kademliaRepositoryNode = (KademliaRepositoryNode<C, K, V>) kademliaNode;
        BoundedHashUtil boundedHashUtil = new BoundedHashUtil(Common.IDENTIFIER_SIZE);
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
}
