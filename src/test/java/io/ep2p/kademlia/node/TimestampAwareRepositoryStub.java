package io.ep2p.kademlia.node;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimestampAwareRepositoryStub extends SampleRepository implements TimestampAwareKademliaRepository<Integer, String> {
    @Override
    public Map<Integer, String> getDataOlderThan(int amount, TimeUnit unit, int size) {
        return this.data;
    }
}
