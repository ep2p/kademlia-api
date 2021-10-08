package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.v4.connection.ConnectionInfo;

public interface Node<ID extends Number, C extends ConnectionInfo> {
    C getConnectionInfo();
    ID getId();
}
