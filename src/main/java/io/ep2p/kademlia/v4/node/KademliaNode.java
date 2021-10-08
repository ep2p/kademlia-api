package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.connection.MessageSender;
import io.ep2p.kademlia.v4.exception.HandlerNotFoundException;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.message.handler.MessageHandler;
import io.ep2p.kademlia.v4.table.Bucket;
import io.ep2p.kademlia.v4.table.RoutingTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class KademliaNode<ID extends Number, C extends ConnectionInfo> implements KademliaNodeAPI<ID, C> {
    @Getter
    private final ID id;
    @Getter
    private final C connectionInfo;
    @Getter
    private final RoutingTable<ID, C, Bucket<ID, C>> routingTable;
    @Getter
    private final MessageSender<ID, C> messageSender;
    private final Map<String, MessageHandler<ID, C>> messageHandlerRegistry = new ConcurrentHashMap<>();


    public KademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender) {
        this.id = id;
        this.connectionInfo = connectionInfo;
        this.routingTable = routingTable;
        this.messageSender = messageSender;
    }

    @Override
    public void start() {
        this.init();
    }

    @Override
    public void start(Node<ID, C> bootstrapNode) {

    }

    @Override
    public void stop() {

    }

    @Override
    public <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, O> onMessage(KademliaMessage<ID, C, I> message) throws HandlerNotFoundException {
        assert message != null;
        MessageHandler<ID, C> messageHandler = messageHandlerRegistry.get(message.getType());
        if (messageHandler == null)
            throw new HandlerNotFoundException(message.getType());
        return messageHandler.handle(this, message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler) {
        this.messageHandlerRegistry.put(type, messageHandler);
    }

    //todo: register message listeners here
    protected final void init(){

    }
}
