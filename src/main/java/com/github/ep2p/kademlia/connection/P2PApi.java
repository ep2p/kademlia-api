package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.Node;

public interface P2PApi<C extends ConnectionInfo> {
    FindNodeAnswer<C> onFindNode(int externalNodeId);
    PingAnswer onPing(Node<C> node);
    void onShutdownSignal(Node<C> node);
}
