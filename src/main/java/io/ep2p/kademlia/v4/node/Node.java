package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

import java.io.Serializable;

public interface Node<ID extends Number, C extends ConnectionInfo> extends Serializable {
    C getConnectionInfo();
    ID getId();
}
