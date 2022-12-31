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

public class FindNodeRequestMessageHandler<I extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<I, C> {

    @Override
    @SuppressWarnings("unchecked")
    protected <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O doHandle(KademliaNodeAPI<I,C> kademliaNode, U message) {
        return (O) handleFindNodeRequestMessage(kademliaNode, (FindNodeRequestMessage<I, C>) message);
    }

    @SneakyThrows
    protected FindNodeResponseMessage<I, C> handleFindNodeRequestMessage(KademliaNodeAPI<I, C> kademliaNode, FindNodeRequestMessage<I, C> message){
        FindNodeAnswer<I, C> findNodeAnswer = kademliaNode.getRoutingTable().findClosest(message.getDestinationId());

        try {
            kademliaNode.getRoutingTable().update(message.getNode());
        } catch (FullBucketException e) {
            RoutingTableUtil.softUpdate(kademliaNode, message.getNode());
        }

        FindNodeResponseMessage<I, C> response = new FindNodeResponseMessage<>();
        response.setData(findNodeAnswer);
        return response;
    }
}
