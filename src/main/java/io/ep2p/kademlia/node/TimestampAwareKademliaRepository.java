package io.ep2p.kademlia.node;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface TimestampAwareKademliaRepository<K,V> extends KademliaRepository<K, V> {
    Map<K, V> getDataOlderThan(int amount, TimeUnit unit, int page, int size);
}
