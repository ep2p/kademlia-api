package io.ep2p.kademlia.exception;


/**
 *  Thrown when message is passed to KademliaNodeAPI but does not have any related registered handler
 */
public class HandlerNotFoundException extends Exception {

    public HandlerNotFoundException(String type) {
        super("No message handler found for type: " + type);
    }

}
