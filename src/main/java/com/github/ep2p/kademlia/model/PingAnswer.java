package com.github.ep2p.kademlia.model;

/**
 * @brief Model for ping reply
 * @param <ID> Number type of node ID between supported types
 */
public class PingAnswer<ID extends Number> extends Answer<ID> {
    public PingAnswer(ID nodeId) {
        setNodeId(nodeId);
        setAlive(true);
    }

    public PingAnswer(ID nodeId, boolean alive) {
        setNodeId(nodeId);
        setAlive(alive);
    }

    public PingAnswer() {
    }
}
