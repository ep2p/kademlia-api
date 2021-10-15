package io.ep2p.kademlia.util;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class FindNodeAnswerReducer<ID extends Number, C extends ConnectionInfo> {
    private final ID nodeId;
    private final FindNodeAnswer<ID, C> findNodeAnswer;
    private final int max;
    private final int identifierSize;

    public FindNodeAnswerReducer(ID nodeId, FindNodeAnswer<ID, C> findNodeAnswer, int max, int identifierSize) {
        this.nodeId = nodeId;
        this.findNodeAnswer = findNodeAnswer;
        this.max = max;
        this.identifierSize = identifierSize;
    }

    public void reduce(){
        List<ExternalNode<ID, C>> nodes = new ArrayList<>();
        List<ExternalNode<ID, C>> answerNodes = this.findNodeAnswer.getNodes();

        for(int i = 0; i < identifierSize; i++){
            if (nodes.size() <= this.max){
                break;
            }

            for (ExternalNode<ID, C> answerNode : answerNodes) {
                if (answerNode.getId().equals(power(nodeId, i))){
                    nodes.add(answerNode);
                    answerNodes.remove(answerNode);
                    break;
                }
            }
        }

        int i = 0;
        while (nodes.size() <= this.max && i < answerNodes.size()){
            nodes.add(answerNodes.get(i));
            i++;
        }


        this.findNodeAnswer.update(nodes);
    }

    @SuppressWarnings("unchecked")
    private ID power(ID in, int power){
        if (in instanceof Integer){
            return (ID) Integer.valueOf(((Integer) in) ^ power);
        }

        if (in instanceof Long){
            return (ID) Long.valueOf(((Long) in) ^ power);
        }

        if (in instanceof BigInteger){
            return (ID) ((BigInteger) in).pow(power);
        }

        throw new IllegalArgumentException();
    }
}
