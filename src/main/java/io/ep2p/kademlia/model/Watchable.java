package io.ep2p.kademlia.model;

import java.util.concurrent.TimeUnit;

/**
 * @brief Watchable is used in async repository to keep waiting for Answer models and then return the object
 */
public interface Watchable {
    /**
     * @brief Watches for an answer without any timeout
     * @throws InterruptedException thrown if watching is interrupted
     */
    void watch() throws InterruptedException;

    /**
     * @brief Watches for an answer with timeout
     * @param val timeout value
     * @param timeUnit unit of timeout
     * @throws InterruptedException thrown if watching is interrupted
     */
    void watch(long val, TimeUnit timeUnit) throws InterruptedException;

    /**
     * @brief Called when watching is over
     */
    void release();
}
