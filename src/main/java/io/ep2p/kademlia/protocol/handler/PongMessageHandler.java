package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.FindNodeRequestMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PongKademliaMessage;
import lombok.var;
import org.jetbrains.annotations.NotNull;

public class PongMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O doHandle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PongKademliaMessage<ID, C>) message);
    }

    protected EmptyKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, @NotNull PongKademliaMessage<ID, C> message){
        if (!message.isAlive()){
            System.out.println(message.getNode() + " is not alive");
            kademliaNode.getRoutingTable().delete(message.getNode());
        }
        try {
            if (kademliaNode.getRoutingTable().update(message.getNode())) {
                FindNodeRequestMessage<ID, C> findNodeRequestMessage = new FindNodeRequestMessage<>();
                findNodeRequestMessage.setData(kademliaNode.getId());
                var response = kademliaNode.getMessageSender().sendMessage(kademliaNode, message.getNode(), findNodeRequestMessage);
                kademliaNode.onMessage(response);
            }
        } catch (FullBucketException e) {
            //TODO: log
        } catch (HandlerNotFoundException e) {
            //TODO
        }
        return new EmptyKademliaMessage<ID, C>();
    }
}
