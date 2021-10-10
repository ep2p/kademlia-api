package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import lombok.SneakyThrows;
import lombok.var;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TestMessageSenderAPI<ID extends Number, C extends ConnectionInfo> implements MessageSender<ID, C> {
    public final Map<ID, KademliaNodeAPI<ID, C>> map = new HashMap<>();

    public void registerNode(KademliaNodeAPI<ID, C> node){
        map.put(node.getId(), node);
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, I> sendMessage(KademliaNodeAPI<ID, C> caller, Node<ID, C> receiver, KademliaMessage<ID, C, O> message) {
        message.setNode(caller);
        var response = (KademliaMessage<ID, C, I>) this.map.get(receiver.getId()).onMessage(message);
        response.setNode(receiver);
        return response;
    }

    public void stopAll(){
        for (KademliaNodeAPI<ID, C> kademliaNodeAPI : this.map.values()) {
            kademliaNodeAPI.stop();
        }
    }
}
