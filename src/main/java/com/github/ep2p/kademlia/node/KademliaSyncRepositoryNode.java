package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.exception.GetException;
import com.github.ep2p.kademlia.exception.StoreException;
import com.github.ep2p.kademlia.model.GetAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

public class KademliaSyncRepositoryNode<C extends ConnectionInfo, K, V> extends KademliaRepositoryNode<C,K,V> {
    private volatile Map<K, StoreAnswer<K>> answerMap = new HashMap<>();
    private volatile Map<K, GetAnswer<K, V>> getMap = new HashMap<>();

    public KademliaSyncRepositoryNode(Integer nodeId, RoutingTable<C> routingTable, NodeConnectionApi<C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository) {
        super(nodeId, routingTable, nodeConnectionApi, connectionInfo, kademliaRepository);
    }

    @SneakyThrows
    @Override
    public StoreAnswer<K> store(K key, V value) throws StoreException {
        StoreAnswer<K> newStoreAnswer = new StoreAnswer<>();
        synchronized (this){
            StoreAnswer<K> kStoreAnswer = answerMap.putIfAbsent(key, newStoreAnswer);
            if(kStoreAnswer != null){
                newStoreAnswer = kStoreAnswer;
            }
        }

        try {
            StoreAnswer<K> storeAnswer = super.store(key, value);
            if(storeAnswer.getAction().equals(StoreAnswer.Action.STORED)){
                return storeAnswer;
            }else {
                storeAnswer = answerMap.get(key);
                while (storeAnswer.getAction() == null){}
            }
            return storeAnswer;
        }finally {
            answerMap.remove(key);
        }
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
            if(getAnswer.getAction().equals(GetAnswer.Action.FOUND)){
                return getAnswer;
            }else {
                getAnswer = getMap.get(key);
                while (getAnswer.getAction() == null){}
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
        getAnswer.setAction(value == null ? GetAnswer.Action.FAILED : GetAnswer.Action.FOUND);
    }

    @Override
    public void onStoreResult(Node<C> node, K key, boolean successful) {
        super.onStoreResult(node, key, successful);
        StoreAnswer<K> kStoreAnswer = answerMap.get(key);
        kStoreAnswer.setAction(successful ? StoreAnswer.Action.STORED : StoreAnswer.Action.FAILED);
        kStoreAnswer.setKey(key);
        kStoreAnswer.setNodeId(node.getId());
        kStoreAnswer.setAlive(true);
    }
}
