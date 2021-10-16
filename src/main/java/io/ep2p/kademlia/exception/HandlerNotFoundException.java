package io.ep2p.kademlia.exception;

public class HandlerNotFoundException extends Exception {

    public HandlerNotFoundException(String type) {
        super("No message handler found for type: " + type);
    }

}
