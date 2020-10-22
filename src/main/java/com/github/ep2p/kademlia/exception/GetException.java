package com.github.ep2p.kademlia.exception;

public class GetException extends Exception {
    public GetException() {
        super("Failed to find a value for key");
    }

    public GetException(Exception e) {
        super(e);
    }
}
