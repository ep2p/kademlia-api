package io.ep2p.kademlia.node;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class TimestampAwareRepositoryStub extends SampleRepository implements TimestampAwareKademliaRepository<Integer, String> {
    private final List<Integer> keys = new CopyOnWriteArrayList<>();

    @Override
    public Map<Integer, String> getDataOlderThan(int amount, TimeUnit unit, int page, int size) {
        List<Integer> page_keys = getPage(keys, page, size);
        Map<Integer, String> result = new HashMap<>();
        for(Integer key: page_keys){
            result.put(key, this.data.get(key));
        }
        return result;
    }

    @Override
    public void store(Integer key, String value) {
        super.store(key, value);
        keys.add(key);
    }

    @Override
    public void remove(Integer key) {
        super.remove(key);
        keys.remove(key);
    }

    private static <T> List<T> getPage(List<T> sourceList, int page, int pageSize) {
        if(pageSize <= 0 || page <= 0) {
            throw new IllegalArgumentException("invalid page size: " + pageSize);
        }

        int fromIndex = (page - 1) * pageSize;
        if(sourceList == null || sourceList.size() <= fromIndex){
            return Collections.emptyList();
        }

        // toIndex exclusive
        return sourceList.subList(fromIndex, Math.min(fromIndex + pageSize, sourceList.size()));
    }
}
