package io.ep2p.kademlia.protocol.handler;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PongKademliaMessage;
import io.ep2p.kademlia.util.RoutingTableUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class PongMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O doHandle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PongKademliaMessage<ID, C>) message);
    }

    protected EmptyKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, @NotNull PongKademliaMessage<ID, C> message){
        try {
            RoutingTableUtil.softUpdate(kademliaNode, message.getNode());
        } catch (HandlerNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return new EmptyKademliaMessage<ID, C>();
    }
}
