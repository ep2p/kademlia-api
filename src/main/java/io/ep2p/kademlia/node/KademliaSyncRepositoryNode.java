package io.ep2p.kademlia.node;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.kademlia.util.KeyHashGenerator;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * KademliaNode child which implements StorageNodeApi (sync)
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 * @param <K> storage key type
 * @param <V> storage value type
 */
public class KademliaSyncRepositoryNode<ID extends Number, C extends ConnectionInfo, K, V> extends KademliaRepositoryNode<ID, C,K,V> {
    private final Map<K, StoreAnswer<ID, K>> storeMap = new ConcurrentHashMap<>();
    private final Map<K, Lock> storeLockMap = new HashMap<>();
    private final Map<K, GetAnswer<ID, K, V>> getMap = new ConcurrentHashMap<>();
    private final Map<K, Lock> getLockMap = new HashMap<>();

    public KademliaSyncRepositoryNode(ID nodeId, RoutingTable<ID, C, Bucket<ID, C>> routingTable, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository) {
        super(nodeId, routingTable, nodeConnectionApi, connectionInfo, kademliaRepository);
    }

    public KademliaSyncRepositoryNode(ID nodeId, RoutingTable<ID, C, Bucket<ID, C>> routingTable, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        super(nodeId, routingTable, nodeConnectionApi, connectionInfo, kademliaRepository, keyHashGenerator);
    }

    /**
     * @param key data key to store
     * @param value data value
     * @param timeout storing timeout
     * @param timeUnit time unit of timeout
     * @return StoreAnswer
     * @throws StoreException if fails to store data
     * @throws InterruptedException if timeout reaches
     */
    @Deprecated
    public StoreAnswer<ID, K> store(K key, V value, long timeout, TimeUnit timeUnit) throws StoreException, InterruptedException {
        return this.store(key, value, timeout, timeUnit, false);
    }

    /**
     * @param key data key to store
     * @param value data value
     * @param timeout storing timeout
     * @param timeUnit time unit of timeout
     * @param force determines if storing should be forced
     * @return StoreAnswer
     * @throws StoreException if fails to store data
     * @throws InterruptedException if timeout reaches
     */
    public StoreAnswer<ID,K> store(K key, V value, long timeout, TimeUnit timeUnit, boolean force) throws StoreException, InterruptedException {
        Lock keyLock = new ReentrantLock();
        synchronized (this){
            Lock oldLock = storeLockMap.putIfAbsent(key, keyLock);
            if(oldLock != null){
                keyLock = oldLock;
            }
        }
        if (keyLock.tryLock()) {
            try {
                StoreAnswer<ID, K> storeAnswer = super.store(key, value, force);
                if(storeAnswer.getResult().equals(StoreAnswer.Result.STORED)){
                    return storeAnswer;
                }else {
                    StoreAnswer<ID, K> watchableStoreAnswer = new StoreAnswer<>();
                    watchableStoreAnswer.setResult(StoreAnswer.Result.TIMEOUT);
                    storeMap.putIfAbsent(key, watchableStoreAnswer);
                    if(timeUnit == null)
                        watchableStoreAnswer.watch();
                    else
                        watchableStoreAnswer.watch(timeout, timeUnit);
                    return watchableStoreAnswer;
                }
            }finally {
                keyLock.unlock();
                storeLockMap.remove(key);
                storeMap.remove(key);
            }
        }else {
            StoreAnswer<ID, K> kWatchableStoreAnswer = storeMap.get(key);
            if(kWatchableStoreAnswer != null){
                if(timeUnit == null)
                    kWatchableStoreAnswer.watch();
                else
                    kWatchableStoreAnswer.watch(timeout, timeUnit);
                return kWatchableStoreAnswer;
            }else {
                throw new StoreException("Key is already under process!");
            }
        }
    }

    /**
     * @param key   Key to store
     * @param value Value to store
     * @return StoreAnswer
     * @throws StoreException if fails to store data
     */
    @Override
    @Deprecated
    public StoreAnswer<ID, K> store(K key, V value) throws StoreException {
        try {
            return this.store(key, value, 0, null, false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public StoreAnswer<ID, K> store(K key, V value, boolean force) throws StoreException {
        try {
            return this.store(key, value, 0, null, force);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param key   Key to get value of
     * @param timeout  timeout for retrieving value
     * @param timeUnit unit of timeout
     * @return GetAnswer
     * @throws GetException if this key is already being asked for
     */
    @SneakyThrows
    public GetAnswer<ID, K, V> get(K key, long timeout, TimeUnit timeUnit) throws GetException {
        Lock keyLock = new ReentrantLock();
        synchronized (this){
            Lock oldLock = getLockMap.putIfAbsent(key, keyLock);
            if(oldLock != null){
                keyLock = oldLock;
            }
        }
        if (keyLock.tryLock()) {
            try {
                GetAnswer<ID, K, V> getAnswer = super.get(key);
                if(getAnswer.getResult().equals(GetAnswer.Result.FOUND) || getAnswer.getResult().equals(GetAnswer.Result.FAILED)) {
                    return getAnswer;
                }else {
                    GetAnswer<ID, K, V> answer = new GetAnswer<>();
                    answer.setResult(GetAnswer.Result.TIMEOUT);
                    getMap.putIfAbsent(key, answer);
                    if(timeUnit == null)
                        answer.watch();
                    else
                        answer.watch(timeout, timeUnit);
                    return answer;
                }
            }finally {
                keyLock.unlock();
                getLockMap.remove(key);
                getMap.remove(key);
            }
        }else {
            GetAnswer<ID, K,V> getAnswer = getMap.get(key);
            if(getAnswer != null){
                if(timeUnit == null)
                    getAnswer.watch();
                else
                    getAnswer.watch(timeout, timeUnit);
                return getAnswer;
            }else {
                throw new GetException("Key is already under process!");
            }
        }
    }

    /**
     * @param key Key for data to look
     * @return GetAnswer
     */
    @SneakyThrows
    @Override
    public GetAnswer<ID, K, V> get(K key) throws GetException {
        return this.get(key, 0, null);
    }

    /**
     * Override of onGetResult to return the answer that was being watched
     * @param node  Data holder node
     * @param key   Key of data
     * @param value Value of data
     */
    @Override
    public void onGetResult(Node<ID, C> node, K key, V value) {
        GetAnswer<ID, K, V> getAnswer = getMap.get(key);
        getAnswer.setNodeId(node.getId());
        getAnswer.setAlive(true);
        getAnswer.setKey(key);
        getAnswer.setValue(value);
        getAnswer.setResult(value == null ? GetAnswer.Result.FAILED : GetAnswer.Result.FOUND);
        getAnswer.release();
        super.onGetResult(node, key, value);
    }

    /**
     * Override of onStoreResult to return the answer that was being watched
     * @param node       Node that holds key
     * @param key        Key itself
     * @param successful if data is stored successfully
     */
    @SneakyThrows
    @Override
    public void onStoreResult(Node<ID, C> node, K key, boolean successful) {
        StoreAnswer<ID, K> kStoreAnswer = storeMap.get(key);
        if (kStoreAnswer == null)
            return;
        kStoreAnswer.setResult(successful ? StoreAnswer.Result.STORED : StoreAnswer.Result.FAILED);
        kStoreAnswer.setKey(key);
        kStoreAnswer.setNodeId(node.getId());
        kStoreAnswer.setAlive(true);
        kStoreAnswer.release();
        super.onStoreResult(node, key, successful);
    }
}
