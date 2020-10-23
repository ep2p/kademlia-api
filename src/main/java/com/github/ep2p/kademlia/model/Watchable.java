package com.github.ep2p.kademlia.model;

import java.util.concurrent.TimeUnit;

public interface Watchable {
    void watch() throws InterruptedException;
    void watch(long val, TimeUnit timeUnit) throws InterruptedException;
    void release();
}
