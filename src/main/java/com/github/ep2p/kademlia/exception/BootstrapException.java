package com.github.ep2p.kademlia.exception;

import com.github.ep2p.kademlia.node.Node;
import lombok.Getter;

@Getter
public class BootstrapException extends Exception {
    private final Node node;

    public BootstrapException(Node node) {
        super("Failed to contact bootstrap node");
        this.node = node;
    }
}
