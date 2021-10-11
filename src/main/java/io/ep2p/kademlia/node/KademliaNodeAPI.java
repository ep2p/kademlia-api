package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface KademliaNodeAPI<ID extends Number, C extends ConnectionInfo> extends Node<ID, C> {
    RoutingTable<ID, C, Bucket<ID, C>> getRoutingTable();
    void start();
    Future<Boolean> start(Node<ID, C> bootstrapNode) throws FullBucketException;
    void stop();
    default void stopNow(){
        stop();
    }
    boolean isRunning();
    MessageSender<ID, C> getMessageSender();
    NodeSettings getNodeSettings();
    KademliaMessage<ID, C, ? extends Serializable> onMessage(KademliaMessage<ID, C, ? extends Serializable> message) throws HandlerNotFoundException;
    void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler);
}
