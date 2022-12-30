package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.MessageHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public interface DHTStoreServiceAPI<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends MessageHandler<ID, C> {
    default void cleanUp(){}
    Future<StoreAnswer<ID, C, K>> store(K key, V value);
    default List<String> getMessageHandlerTypes(){
        return new ArrayList<>(Arrays.asList(MessageType.DHT_STORE, MessageType.DHT_STORE_RESULT));
    }
}
