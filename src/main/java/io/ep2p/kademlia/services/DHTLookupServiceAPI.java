package io.ep2p.kademlia.services;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.protocol.handler.MessageHandler;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DHTLookupServiceAPI<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends MessageHandler<ID, C> {
    default void cleanUp(){}
    Future<LookupAnswer<ID, C, K, V>> lookup(K key);
}
