package io.ep2p.kademlia.exception;

/**
 * Exception thrown when storing data fails cause its already under process
 */
public class DuplicateStoreRequest extends Exception {
    public DuplicateStoreRequest() {
        super("Store request is duplicate and is already under process");
    }
}
