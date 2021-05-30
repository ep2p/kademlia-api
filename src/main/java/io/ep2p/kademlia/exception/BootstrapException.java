package io.ep2p.kademlia.exception;

import io.ep2p.kademlia.node.Node;
import lombok.Getter;

/**
 * Exception thrown on node bootstrap failure
 */
@Getter
public class BootstrapException extends Exception {
    private final Node node;

    public BootstrapException(Node node, String message, Exception e){
        super(message, e);
        this.node = node;
    }

    public BootstrapException(Node node, String message){
        this(node, message, null);
    }

    public BootstrapException(Node node) {
        this(node, "Failed to contact bootstrap node");
    }
}
