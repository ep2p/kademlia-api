package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;

import java.io.Serializable;
import java.util.Date;

public interface Node<ID extends Number, C extends ConnectionInfo> extends Serializable {
    C getConnectionInfo();
    ID getId();
    void setLastSeen(Date date);
    default Date getLastSeen(){
        return new Date();
    }
}
