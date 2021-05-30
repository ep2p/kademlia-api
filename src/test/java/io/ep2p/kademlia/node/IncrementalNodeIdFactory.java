package io.ep2p.kademlia.node;

public class IncrementalNodeIdFactory implements NodeIdFactory {
    private volatile int current = 0;

    @Override
    public synchronized Integer getNodeId() {
        return current++;
    }
}
