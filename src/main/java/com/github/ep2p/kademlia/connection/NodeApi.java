package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.exception.NodeIsOfflineException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.Node;

public interface NodeApi<C extends ConnectionInfo> {
    FindNodeAnswer<C> onFindNode(int externalNodeId) throws NodeIsOfflineException;
    PingAnswer onPing(Node<C> node) throws NodeIsOfflineException;
    void onShutdownSignal(Node<C> node);
}
