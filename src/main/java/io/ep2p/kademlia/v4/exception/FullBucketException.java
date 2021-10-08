package io.ep2p.kademlia.v4.exception;

/**
 * Exception thrown when storing data on node fails
 */
public class FullBucketException extends Exception {
    public FullBucketException() {
        super("Bucket is full");
    }
}
