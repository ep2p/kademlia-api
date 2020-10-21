package com.github.ep2p.kademlia.model;

public class PingAnswer extends Answer {
    public PingAnswer(int nodeId) {
        setNodeId(nodeId);
        setAlive(true);
    }
}
