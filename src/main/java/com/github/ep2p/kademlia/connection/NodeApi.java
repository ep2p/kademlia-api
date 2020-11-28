package com.github.ep2p.kademlia.connection;

import com.github.ep2p.kademlia.exception.NodeIsOfflineException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.node.Node;

public interface NodeApi<ID extends Number, C extends ConnectionInfo> {
    FindNodeAnswer<ID, C> onFindNode(Node<ID, C> node, ID externalNodeId) throws NodeIsOfflineException;
    PingAnswer onPing(Node<ID, C> node) throws NodeIsOfflineException;
    void onShutdownSignal(Node<ID, C> node);
}
