package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.strategies.ReferencedNodesStrategy;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.Future;

public abstract class KademliaNodeAPIDecorator<I extends Number, C extends ConnectionInfo> implements KademliaNodeAPI<I, C> {
    @Getter
    private final KademliaNodeAPI<I, C> kademliaNode;

    protected KademliaNodeAPIDecorator(KademliaNodeAPI<I, C> kademliaNode) {
        this.kademliaNode = kademliaNode;
    }

    @Override
    public RoutingTable<I, C, Bucket<I, C>> getRoutingTable() {
        return this.getKademliaNode().getRoutingTable();
    }

    @Override
    public void start() {
        this.getKademliaNode().start();
    }

    @Override
    public Future<Boolean> start(Node<I, C> bootstrapNode) {
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
    public KademliaMessage<I, C, ? extends Serializable> onMessage(KademliaMessage<I, C, ? extends Serializable> message) throws HandlerNotFoundException {
        return this.getKademliaNode().onMessage(message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<I, C> messageHandler) {
        this.getKademliaNode().registerMessageHandler(type, messageHandler);
    }

    @Override
    public MessageSender<I, C> getMessageSender() {
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
    public I getId() {
        return this.getKademliaNode().getId();
    }

    @Override
    public MessageHandler<I, C> getHandler(String type) throws HandlerNotFoundException {
        return this.getKademliaNode().getHandler(type);
    }

    @Override
    public void setReferencedNodesStrategy(ReferencedNodesStrategy referencedNodesStrategy) {
        this.getKademliaNode().setReferencedNodesStrategy(referencedNodesStrategy);
    }
}
