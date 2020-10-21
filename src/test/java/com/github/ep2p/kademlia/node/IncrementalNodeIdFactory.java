package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.node.NodeIdFactory;

public class IncrementalNodeIdFactory implements NodeIdFactory {
    private volatile int current = 0;

    @Override
    public synchronized Integer getNodeId() {
        return current++;
    }
}
