package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.message.KademliaMessage;
import io.ep2p.kademlia.message.handler.MessageHandler;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
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
    public void isRunning() {
        this.getKademliaNode().isRunning();
    }

    @Override
    public void stop() {
        this.getKademliaNode().stop();
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
}
