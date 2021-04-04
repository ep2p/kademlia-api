package com.github.ep2p.kademlia.model;

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
