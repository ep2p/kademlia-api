package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.BigIntegerExternalNode;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.node.external.IntegerExternalNode;
import io.ep2p.kademlia.node.external.LongExternalNode;

import java.math.BigInteger;

@SuppressWarnings("unchecked")
public class BucketHelper {

    public static <ID extends Number, C extends ConnectionInfo> void addToAnswer(Bucket<ID, C> bucket, FindNodeAnswer<ID, C> answer, ID destination) {
        if(bucket instanceof LongBucket){
            for (long id : ((Bucket<Long, C>) bucket).getNodeIds()) {
                Node<Long, C> node = ((Bucket<Long, C>) bucket).getNode(id);
                long destination1 = (Long) destination;
                answer.add((ExternalNode<ID, C>) new LongExternalNode<C>(node,id ^ destination1));
            }
        }

        if(bucket instanceof IntegerBucket){
            for (int id : ((Bucket<Integer, C>) bucket).getNodeIds()) {
                Node<Integer, C> node = ((Bucket<Integer, C>) bucket).getNode(id);
                int destination1 = (Integer) destination;
                answer.add((ExternalNode<ID, C>) new IntegerExternalNode<C>(node,id ^ destination1));
            }
        }

        if(bucket instanceof BigIntegerBucket){
            for (BigInteger id : ((Bucket<BigInteger, C>) bucket).getNodeIds()) {
                Node<BigInteger, C> node = ((Bucket<BigInteger, C>) bucket).getNode(id);
                BigInteger destination1 = (BigInteger) destination;
                answer.add((ExternalNode<ID, C>) new BigIntegerExternalNode<C>(node,destination1.xor(id)));
            }
        }

    }

}
