package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.MessageHandler;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public interface DHTLookupServiceAPI<I extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends MessageHandler<I, C> {
    default void cleanUp(){}
    Future<LookupAnswer<I, C, K, V>> lookup(K key);
    default List<String> getMessageHandlerTypes(){
        return Arrays.asList(MessageType.DHT_LOOKUP, MessageType.DHT_LOOKUP_RESULT);
    }
}
