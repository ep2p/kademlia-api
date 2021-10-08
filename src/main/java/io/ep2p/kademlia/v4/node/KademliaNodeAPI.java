package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.message.handler.MessageHandler;
import io.ep2p.kademlia.v4.table.Bucket;
import io.ep2p.kademlia.v4.table.RoutingTable;

import java.io.Serializable;

public interface KademliaNodeAPI<ID extends Number, C extends ConnectionInfo> extends Node<ID, C> {
    RoutingTable<ID, C, Bucket<ID, C>> getRoutingTable();
    void start();
    void start(Node<ID, C> bootstrapNode);
    void stop();
    <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, O> onMessage(KademliaMessage<ID, C, I> message);
    void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler);
}