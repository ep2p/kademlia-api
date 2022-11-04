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

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * Basic KademliaNodeAPI, contains core functionality of any sort of KademliaNode
 * @param <ID> Type of the node ID
 * @param <C> Type of the node ConnectionInfo
 */
public interface KademliaNodeAPI<ID extends Number, C extends ConnectionInfo> extends Node<ID, C> {
    /**
     * @return RoutingTable set for this node
     */
    RoutingTable<ID, C, Bucket<ID, C>> getRoutingTable();
    /**
     *  Starts the node. Shall be already bootstrapped.
     */
    void start();
    /**
     * @param bootstrapNode Node information to bootstrap this node with then starts
     * @return Boolean Future, determines if node was bootstrapped successfully
     */
    Future<Boolean> start(Node<ID, C> bootstrapNode);
    /**
     *  Stop the node, gracefully
     */
    void stop();
    /**
     *  Stop the node with force immediately
     */
    default void stopNow(){
        stop();
    }
    /**
     * @return boolean, if node is in running state
     */
    boolean isRunning();
    /**
     * @return MessageSender set for this node
     */
    MessageSender<ID, C> getMessageSender();
    /**
     * @return NodeSettings set for this node
     */
    NodeSettings getNodeSettings();
    /**
     * Called when there is a message that this node should handle
     * @param message message to handle
     * @return (optional) response message after handling
     * @throws HandlerNotFoundException when no handler is found for the message
     */
    KademliaMessage<ID, C, ? extends Serializable> onMessage(KademliaMessage<ID, C, ? extends Serializable> message) throws HandlerNotFoundException;
    /**
     * @param type type of the message that should be handled by messageHandler
     * @param messageHandler to handle the message
     */
    void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler);

    /**
     * @param type type of the message to get the handler of
     * @return MessageHandler that is registered for the type
     * @throws HandlerNotFoundException when no handler is registered for type of the message
     */
    MessageHandler<ID, C> getHandler(String type) throws HandlerNotFoundException;

    void setReferencedNodesStrategy(ReferencedNodesStrategy referencedNodesStrategy);
}
