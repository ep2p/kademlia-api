package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.node.ExternalNode;

public class BucketHelper {

    public static <C extends ConnectionInfo> void addToAnswer(Bucket<C> bucket, FindNodeAnswer<C> answer, int destination) {
        for (int id : bucket.getNodeIds()) {
            answer.getNodes().add(new ExternalNode<C>(bucket.getNode(id),id ^ destination));
        }
    }

}
