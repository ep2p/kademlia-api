package io.ep2p.kademlia.exception;

/**
 * Exception thrown when storing data on node fails
 */
public class StoreException extends Exception {
    public StoreException() {
        super("Failed to store key value");
    }

    public StoreException(String message) {
        super(message);
    }

    public StoreException(Exception e) {
        super(e);
    }
}
