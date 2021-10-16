package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.Future;

public abstract class KademliaNodeAPIDecorator<ID extends Number, C extends ConnectionInfo> implements KademliaNodeAPI<ID, C> {
    @Getter
    private final KademliaNodeAPI<ID, C> kademliaNode;

    public KademliaNodeAPIDecorator(KademliaNodeAPI<ID, C> kademliaNode) {
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
    public Future<Boolean> start(Node<ID, C> bootstrapNode) {
        return this.getKademliaNode().start(bootstrapNode);
    }

    @Override
    public boolean isRunning() {
        return this.getKademliaNode().isRunning();
    }

    @Override
    public void stop() {
        this.getKademliaNode().stop();
    }

    @Override
    public void stopNow() {
        this.getKademliaNode().stopNow();
    }

    @Override
    public KademliaMessage<ID, C, ? extends Serializable> onMessage(KademliaMessage<ID, C, ? extends Serializable> message) throws HandlerNotFoundException {
        return this.getKademliaNode().onMessage(message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler) {
        this.getKademliaNode().registerMessageHandler(type, messageHandler);
    }

    @Override
    public MessageSender<ID, C> getMessageSender() {
        return this.getKademliaNode().getMessageSender();
    }

    @Override
    public NodeSettings getNodeSettings() {
        return this.getKademliaNode().getNodeSettings();
    }

    @Override
    public C getConnectionInfo() {
        return this.getKademliaNode().getConnectionInfo();
    }

    @Override
    public ID getId() {
        return this.getKademliaNode().getId();
    }

    @Override
    public MessageHandler<ID, C> getHandler(String type) throws HandlerNotFoundException {
        return this.getKademliaNode().getHandler(type);
    }
}
