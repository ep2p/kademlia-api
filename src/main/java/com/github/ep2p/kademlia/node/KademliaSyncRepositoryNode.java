package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.exception.GetException;
import com.github.ep2p.kademlia.exception.StoreException;
import com.github.ep2p.kademlia.model.GetAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.model.WatchableStoreAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.ep2p.kademlia.model.StoreAnswer.Result.TIMEOUT;

public class KademliaSyncRepositoryNode<C extends ConnectionInfo, K, V> extends KademliaRepositoryNode<C,K,V> {
    private volatile Map<K, WatchableStoreAnswer<K>> storeMap = new HashMap<>();
    private volatile Map<K, Lock> storeLockMap = new HashMap<>();
    private volatile Map<K, GetAnswer<K, V>> getMap = new HashMap<>();

    public KademliaSyncRepositoryNode(Integer nodeId, RoutingTable<C> routingTable, NodeConnectionApi<C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository) {
        super(nodeId, routingTable, nodeConnectionApi, connectionInfo, kademliaRepository);
    }

    public StoreAnswer<K> store(K key, V value, long timeout, TimeUnit timeUnit) throws StoreException, InterruptedException {
        Lock keyLock = new ReentrantLock();
        synchronized (this){
            Lock oldLock = storeLockMap.putIfAbsent(key, keyLock);
            if(oldLock != null){
                keyLock = oldLock;
            }
        }
        if (keyLock.tryLock()) {
            try {
                StoreAnswer<K> storeAnswer = super.store(key, value);
                if(storeAnswer.getResult().equals(StoreAnswer.Result.STORED)){
                    return storeAnswer;
                }else {
                    WatchableStoreAnswer<K> watchableStoreAnswer = new WatchableStoreAnswer<>();
                    watchableStoreAnswer.setResult(TIMEOUT);
                    storeMap.putIfAbsent(key, watchableStoreAnswer);
                    if(timeUnit == null)
                        watchableStoreAnswer.watch();
                    else
                        watchableStoreAnswer.watch(timeout, timeUnit);
                    storeMap.remove(key);
                    return watchableStoreAnswer;
                }
            }finally {
                keyLock.unlock();
                storeLockMap.remove(key);
            }
        }else {
            WatchableStoreAnswer<K> kWatchableStoreAnswer = storeMap.get(key);
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

    @Override
    @SneakyThrows
    public StoreAnswer<K> store(K key, V value) throws StoreException {
        return this.store(key, value, 0, null);
    }

    @SneakyThrows
    @Override
    public GetAnswer<K, V> get(K key) throws GetException {
        GetAnswer<K, V> newGetAnswer = new GetAnswer<>();
        synchronized (this){
            GetAnswer<K, V> oldGetAnswer = getMap.putIfAbsent(key, newGetAnswer);
            if(oldGetAnswer != null){
                newGetAnswer = oldGetAnswer;
            }
        }

        try {
            GetAnswer<K, V> getAnswer = super.get(key);
            if(getAnswer.getResult().equals(GetAnswer.Result.FOUND)){
                return getAnswer;
            }else {
                getAnswer = getMap.get(key);
                while (getAnswer.getResult() == null){}
            }

            return getAnswer;
        }finally {
            getMap.remove(key);
        }
    }

    @Override
    public void onGetResult(Node<C> node, K key, V value) {
        super.onGetResult(node, key, value);
        GetAnswer<K, V> getAnswer = getMap.get(key);
        getAnswer.setNodeId(node.getId());
        getAnswer.setAlive(true);
        getAnswer.setKey(key);
        getAnswer.setValue(value);
        getAnswer.setResult(value == null ? GetAnswer.Result.FAILED : GetAnswer.Result.FOUND);
    }

    @SneakyThrows
    @Override
    public void onStoreResult(Node<C> node, K key, boolean successful) {
        super.onStoreResult(node, key, successful);
        WatchableStoreAnswer<K> kStoreAnswer = storeMap.get(key);
        kStoreAnswer.setResult(successful ? StoreAnswer.Result.STORED : StoreAnswer.Result.FAILED);
        kStoreAnswer.setKey(key);
        kStoreAnswer.setNodeId(node.getId());
        kStoreAnswer.setAlive(true);
        kStoreAnswer.release();
    }
}
