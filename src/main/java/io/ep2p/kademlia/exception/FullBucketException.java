package io.ep2p.kademlia.exception;

/**
 * Exception thrown when storing data on node fails
 */
public class FullBucketException extends Exception {
    public FullBucketException() {
        super("Bucket is full");
    }
}
