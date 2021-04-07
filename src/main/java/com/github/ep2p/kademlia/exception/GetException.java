package com.github.ep2p.kademlia.exception;

/**
 * @brief Exception thrown when there is a failure in getting value of a key
 */
public class GetException extends Exception {
    public GetException() {
        super("Failed to find a value for key");
    }

    public GetException(Exception e) {
        super(e);
    }

    public GetException(String message) {
        super(message);
    }
}
