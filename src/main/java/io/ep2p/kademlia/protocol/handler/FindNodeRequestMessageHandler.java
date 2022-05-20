package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.FindNodeRequestMessage;
import io.ep2p.kademlia.protocol.message.FindNodeResponseMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.util.RoutingTableUtil;
import lombok.SneakyThrows;

import java.io.Serializable;

public class FindNodeRequestMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ? extends Serializable>, O extends KademliaMessage<ID, C, ? extends Serializable>> O doHandle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (FindNodeRequestMessage<ID, C>) message);
    }

    @SneakyThrows
    protected FindNodeResponseMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, FindNodeRequestMessage<ID, C> message){
        FindNodeAnswer<ID, C> findNodeAnswer = kademliaNode.getRoutingTable().findClosest(message.getDestinationId());

        try {
            kademliaNode.getRoutingTable().update(message.getNode());
        } catch (FullBucketException e) {
            RoutingTableUtil.softUpdate(kademliaNode, message.getNode());
        }

        FindNodeResponseMessage<ID, C> response = new FindNodeResponseMessage<ID, C>();
        response.setData(findNodeAnswer);
        return response;
    }
}
