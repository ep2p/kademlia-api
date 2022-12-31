package io.ep2p.kademlia.helpers;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.decorators.DateAwareNodeDecorator;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMessageSenderAPI<I extends Number, C extends ConnectionInfo> implements MessageSender<I, C> {
    public final Map<I, KademliaNodeAPI<I, C>> map = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void registerNode(KademliaNodeAPI<I, C> node){
        map.put(node.getId(), node);
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public <U extends Serializable, O extends Serializable> KademliaMessage<I, C, O> sendMessage(KademliaNodeAPI<I, C> caller, Node<I, C> receiver, KademliaMessage<I, C, U> message) {
        if (!this.map.containsKey(receiver.getId())){
            EmptyKademliaMessage<I, C> kademliaMessage = new EmptyKademliaMessage<>();
            kademliaMessage.setAlive(false);
            kademliaMessage.setNode(receiver);
            return (KademliaMessage<I, C, O>) kademliaMessage;
        }

        message.setNode(new DateAwareNodeDecorator<>(caller));
        var response = (KademliaMessage<I, C, O>) this.map.get(receiver.getId()).onMessage(message);
        response.setNode(new DateAwareNodeDecorator<>(receiver));
        return response;
    }

    @SneakyThrows
    @Override
    public <O extends Serializable> void sendAsyncMessage(KademliaNodeAPI<I, C> caller, Node<I, C> receiver, KademliaMessage<I, C, O> message) {
        message.setNode(caller);
        this.executorService.submit(() -> {
            try {
                map.get(receiver.getId()).onMessage(message);
            } catch (HandlerNotFoundException ignored) {}
        });
    }

    public void stopAll(){
        for (KademliaNodeAPI<I, C> kademliaNodeAPI : this.map.values()) {
            kademliaNodeAPI.stopNow();
        }
    }
}
