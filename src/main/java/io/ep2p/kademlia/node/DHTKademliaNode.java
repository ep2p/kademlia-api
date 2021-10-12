package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class DHTKademliaNode<ID extends Number, C extends ConnectionInfo> extends KademliaNode<ID, C> implements DHTKademliaNodeAPI<ID, C>, MessageHandler<ID, C> {

    public DHTKademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings) {
        super(id, connectionInfo, routingTable, messageSender, nodeSettings);
        this.initDHTKademliaNode();
    }

    @Override
    public <K extends Serializable, V extends Serializable> StoreAnswer<ID, K> store(K key, V value, int timeoutVal, TimeUnit timeoutUnit) {
        return null;
    }

    @Override
    public <K extends Serializable, V extends Serializable> StoreAnswer<ID, K> get(K key, int timeoutVal, TimeUnit timeoutUnit) {
        return null;
    }

    protected void initDHTKademliaNode(){
        this.registerMessageHandler(MessageType.DHT_LOOKUP, this);
        this.registerMessageHandler(MessageType.DHT_LOOKUP_RESULT, this);
        this.registerMessageHandler(MessageType.DHT_STORE, this);
        this.registerMessageHandler(MessageType.DHT_STORE_RESULT, this);
    }


    // TODO
    @Override
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return null;
    }
}
