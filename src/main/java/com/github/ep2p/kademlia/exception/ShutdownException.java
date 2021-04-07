package com.github.ep2p.kademlia.exception;

/**
 * Exception thrown when node shutdown fails
 */
public class ShutdownException extends Exception {
    public ShutdownException(Throwable cause) {
        super(cause);
    }
}
