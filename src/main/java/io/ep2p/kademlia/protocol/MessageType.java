package io.ep2p.kademlia.protocol;

public interface MessageType {
    String EMPTY = "EMPTY";
    String FIND_NODE_REQ = "FIND_NODE_REQ";
    String FIND_NODE_RES = "FIND_NODE_RES";
    String PING = "PING";
    String PONG = "PONG";
    String SHUTDOWN = "SHUTDOWN";
}
