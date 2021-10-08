package io.ep2p.kademlia.message.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.message.FindNodeRequestMessage;
import io.ep2p.kademlia.message.FindNodeResponseMessage;
import io.ep2p.kademlia.message.KademliaMessage;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import lombok.var;

import java.io.Serializable;

public class FindNodeRequestMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ? extends Serializable>, O extends KademliaMessage<ID, C, ? extends Serializable>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (FindNodeRequestMessage<ID, C>) message);
    }

    protected FindNodeResponseMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, FindNodeRequestMessage<ID, C> message){
        try {
            kademliaNode.getRoutingTable().update(message.getNode());
        } catch (FullBucketException e) {
            // TODO
        }

        FindNodeAnswer<ID, C> findNodeAnswer = kademliaNode.getRoutingTable().findClosest(message.getDestinationId());
        var response = new FindNodeResponseMessage<ID, C>();
        response.setData(findNodeAnswer);
        return response;
    }
}
