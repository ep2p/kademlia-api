package com.github.ep2p.kademlia.util;

public class BoundedHashUtil {
    public final int maxSize;

    public BoundedHashUtil(int maxSizeInBits) {
        this.maxSize = maxSizeInBits;
    }

    public int hash(int input){
        return input << -maxSize >>> -maxSize;
    }
}
