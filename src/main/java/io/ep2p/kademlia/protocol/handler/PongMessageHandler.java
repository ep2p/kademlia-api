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
public class PongMessageHandler<I extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<I, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <U extends KademliaMessage<I, C, ?>, O extends KademliaMessage<I, C, ?>> O doHandle(KademliaNodeAPI<I, C> kademliaNode, U message) {
        return (O) doHandle(kademliaNode, (PongKademliaMessage<I, C>) message);
    }

    protected EmptyKademliaMessage<I, C> doHandle(KademliaNodeAPI<I, C> kademliaNode, @NotNull PongKademliaMessage<I, C> message){
        try {
            RoutingTableUtil.softUpdate(kademliaNode, message.getNode());
        } catch (HandlerNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return new EmptyKademliaMessage<>();
    }
}
