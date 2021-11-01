package io.ep2p.kademlia.model;

import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;

public class WatchableAnswer<ID extends Number> extends Answer<ID> {
    private CountDownLatch countDownLatch;

    @SneakyThrows
    public void watch(){
        synchronized (this){
            if (this.countDownLatch == null){
                this.countDownLatch = new CountDownLatch(1);
            }
        }
        this.countDownLatch.await();
    }

    public synchronized void finishWatch(){
        if (this.countDownLatch != null){
            this.countDownLatch.countDown();
            this.countDownLatch = null;
        }
    }
    
}
