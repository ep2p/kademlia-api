package com.github.ep2p.kademlia.model;

import java.util.concurrent.TimeUnit;

public class WatchableStoreAnswer<K> extends StoreAnswer<K> {
    private volatile boolean watching;
    public void watch() throws InterruptedException {
        watching = true;
        synchronized (this){
            while (watching){
                this.wait();
            }
        }
    }

    public void watch(long val, TimeUnit timeUnit) throws InterruptedException {
        watching = true;
        synchronized (this){
            this.wait(timeUnit.toMillis(val));
        }
    }

    public void release() {
        synchronized (this){
            watching = false;
            this.notifyAll();
        }
    }
}
