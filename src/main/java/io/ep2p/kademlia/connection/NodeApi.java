package io.ep2p.kademlia.connection;

import io.ep2p.kademlia.exception.NodeIsOfflineException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.PingAnswer;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.v4.connection.ConnectionInfo;

/**
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
public interface NodeApi<ID extends Number, C extends ConnectionInfo> {
    /**
     * @param node Reference to caller node object
     * @param externalNodeId Destination id for node to find out
     * @return FindNodeAnswer
     * @throws NodeIsOfflineException when alive is false on node
     */
    FindNodeAnswer<ID, C> onFindNode(Node<ID, C> node, ID externalNodeId) throws NodeIsOfflineException;

    /**
     * @param node Reference to caller node object
     * @return PingAnswer
     * @throws NodeIsOfflineException when current node is not running (has stopped of has never started)
     */
    PingAnswer onPing(Node<ID, C> node) throws NodeIsOfflineException;

    /**
     * Caller calls this method to send a signal that they (caller) is shutting down
     * @param node Reference to caller node object
     */
    void onShutdownSignal(Node<ID, C> node);
}
