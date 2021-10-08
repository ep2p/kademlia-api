package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.message.handler.MessageHandler;
import io.ep2p.kademlia.v4.table.Bucket;
import io.ep2p.kademlia.v4.table.RoutingTable;
import lombok.Getter;

import java.io.Serializable;

public abstract class KademliaNodeAPIDecorator<ID extends Number, C extends ConnectionInfo> implements KademliaNodeAPI<ID, C> {
    @Getter
    private final KademliaNodeAPI<ID, C> kademliaNode;

    protected KademliaNodeAPIDecorator(KademliaNodeAPI<ID, C> kademliaNode) {
        this.kademliaNode = kademliaNode;
    }

    @Override
    public RoutingTable<ID, C, Bucket<ID, C>> getRoutingTable() {
        return this.getKademliaNode().getRoutingTable();
    }

    @Override
    public void start() {
        this.getKademliaNode().start();
    }

    @Override
    public void start(Node<ID, C> bootstrapNode) {
        this.getKademliaNode().start(bootstrapNode);
    }

    @Override
    public void stop() {
        this.getKademliaNode().stop();
    }

    @Override
    public <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, O> onMessage(KademliaMessage<ID, C, I> message) {
        return this.getKademliaNode().onMessage(message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler) {
        this.getKademliaNode().registerMessageHandler(type, messageHandler);
    }
}
