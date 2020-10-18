package com.github.ep2p.kademlia.exception;

import com.github.ep2p.kademlia.node.Node;
import lombok.Getter;

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
