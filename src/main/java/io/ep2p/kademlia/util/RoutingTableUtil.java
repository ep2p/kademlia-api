package io.ep2p.kademlia.util;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.kademlia.table.Bucket;

import java.io.Serializable;

public class RoutingTableUtil {

    public static  <ID extends Number, C extends ConnectionInfo> boolean softUpdate(KademliaNodeAPI<ID, C> node, Node<ID, C> nodeToAdd) throws HandlerNotFoundException {
        try {
            return node.getRoutingTable().update(node);
        } catch (FullBucketException e) {
            Bucket<ID, C> bucket = node.getRoutingTable().findBucket(node.getId());
            for (ID nodeId : bucket.getNodeIds()) {
                KademliaMessage<ID, C, Serializable> response = node.getMessageSender().sendMessage(node, bucket.getNode(nodeId), new PingKademliaMessage<>());
                node.onMessage(response);
                if (!response.isAlive()){
                    bucket.remove(nodeId);
                    bucket.add(node.getRoutingTable().getExternalNode(node));
                    break;
                }
            }
            return true;
        }
    }

}
