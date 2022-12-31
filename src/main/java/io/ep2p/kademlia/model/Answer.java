package io.ep2p.kademlia.model;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Model to hold base API call reply answer
 * @param <I> Number type of node ID between supported types
 */
@Getter
@Setter
public class Answer<I extends Number, C extends ConnectionInfo> implements Serializable {
    private Node<I, C> node;
    private boolean isAlive;
}
