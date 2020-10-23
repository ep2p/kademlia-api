package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.connection.StorageNodeApi;
import com.github.ep2p.kademlia.exception.GetException;
import com.github.ep2p.kademlia.exception.StoreException;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.GetAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.table.RoutingTable;
import com.github.ep2p.kademlia.util.BoundedHashUtil;
import lombok.Getter;

import java.util.Date;
import java.util.List;

import static com.github.ep2p.kademlia.Common.LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE;
import static com.github.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;

public class KademliaRepositoryNode<C extends ConnectionInfo, K, V> extends KademliaNode<C> implements StorageNodeApi<C, K, V> {
    @Getter
    private final KademliaRepository<K,V> kademliaRepository;
    private final BoundedHashUtil boundedHashUtil;

    public KademliaRepositoryNode(Integer nodeId, RoutingTable<C> routingTable, NodeConnectionApi<C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository) {
        super(nodeId, routingTable, nodeConnectionApi, connectionInfo);
        this.kademliaRepository = kademliaRepository;
        boundedHashUtil = new BoundedHashUtil(Common.IDENTIFIER_SIZE);
    }

    @Override
    public KademliaNodeListener<C, K, V> getKademliaNodeListener() {
        return (KademliaNodeListener<C, K, V>) super.getKademliaNodeListener();
    }

    /* Node Connection API */

    /**
     * Handlers incoming `Get` requests from other nodes
     * @param callerNode Node that passed info to current node
     * @param requester Node that looks for info
     * @param key Key of data
     */
    @Override
    public void onGetRequest(Node<C> callerNode, Node<C> requester, K key){
        //Check repository for key, if it exists return it
        if(kademliaRepository.contains(key)){
            V value = kademliaRepository.get(key);
            getNodeConnectionApi().sendGetResults(this, requester, key, value);
            return;
        }

        GetAnswer<K, V> getAnswer = findClosestNodesToGetData(requester, key, callerNode);
        if(getAnswer == null)
            getNodeConnectionApi().sendGetResults(this, requester, key, null);
    }

    /* Node Connection Api */

    /**
     * Called when store request enters this requester
     * @param caller Node that passed this request to current node
     * @param requester Node that requested to store the data
     * @param key Data key
     * @param value Data value
     */
    @Override
    public void onStoreRequest(Node<C> caller, Node<C> requester, K key, V value) {
        int hash = boundedHashUtil.hash(key.hashCode());
        //if current requester should persist data, store data and tell requester about it
        if(getId() == hash){
            kademliaRepository.store(key, value);
            getNodeConnectionApi().sendStoreResults(this, requester, key, true);
        //otherwise find closest nodes to store data
        } else {
            FindNodeAnswer<C> findNodeAnswer = getRoutingTable().findClosest(hash);
            if (findClosestNodesToStoreData(requester, findNodeAnswer.getNodes(), key, value, caller) == null) {
                getNodeConnectionApi().sendStoreResults(this, requester, key, false);
            }
        }
    }

    /* Managing API */

    /**
     * @param key Key to store
     * @param value Value to store
     * @return Storing result "STORED: when current node stores data" , "PASSED: When storing request is passed to other nodes"
     * @throws StoreException thrown when no responsible node was found
     */
    public StoreAnswer<K> store(K key, V value) throws StoreException {
        if(!isRunning())
            throw new StoreException("Node is shutting down");
        StoreAnswer<K> storeAnswer = null;
        int hash = boundedHashUtil.hash(key.hashCode());
        //if current requester should persist data, do it immediatly
        if(getId() == hash) {
            kademliaRepository.store(key, value);
            storeAnswer = getNewStoreAnswer(key, StoreAnswer.Action.STORED, this);
        }else {
            FindNodeAnswer<C> findNodeAnswer = getRoutingTable().findClosest(hash);
            storeAnswer = findClosestNodesToStoreData(this, findNodeAnswer.getNodes(), key, value, null);
        }

        if(storeAnswer == null){
            throw new StoreException();
        }

        return storeAnswer;
    }

    /**
     * @param key Key for data to look
     * @return GetAnswer, "Found: current node holds data and returned it", "PASSED: Current node doesnt hold data so request is passed to other nodes"
     * @throws GetException No responsible node found for key
     */
    public GetAnswer<K, V> get(K key) throws GetException {
        if(!isRunning())
            throw new GetException("Node is shutting down");
        //Check repository for key, if it exists return it
        if(kademliaRepository.contains(key)){
            V value = kademliaRepository.get(key);
            return getNewGetAnswer(key, value, GetAnswer.Action.FOUND, this);
        }

        GetAnswer<K,V> getAnswer = null;

        //Otherwise, ask closest node we know to key
        getAnswer = findClosestNodesToGetData(this, key, null);

        if(getAnswer == null){
            throw new GetException();
        }

        return getAnswer;
    }


    /**
     * Called when response to a get request arrives
     * @param node Data holder node
     * @param key Key of data
     * @param value Value of data
     */
    @Override
    public void onGetResult(Node<C> node, K key, V value) {
        getKademliaNodeListener().onKeyLookupResult(this, node, key, value);
    }

    /**
     * Called when a key value is stored in network
     * @param node Node that holds key
     * @param key Key itself
     */
    @Override
    public void onStoreResult(Node<C> node, K key, boolean successful) {
        getKademliaNodeListener().onKeyStoredResult(this, node, key, successful);
    }


    /* --- */

    protected StoreAnswer<K> findClosestNodesToStoreData(Node<C> requester, List<ExternalNode<C>> externalNodeList, K key, V value, Node<C> nodeToIgnore){
        Date date = getDateOfSecondsAgo(LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE);
        StoreAnswer<K> storeAnswer = null;
        for (ExternalNode<C> externalNode : externalNodeList) {
            //if current requester is closest requester store the value
            if(externalNode.getId() == getId()){
                kademliaRepository.store(key, value);
                storeAnswer = getNewStoreAnswer(key, StoreAnswer.Action.STORED, this);
                //if requester of storing is not current node, tell them about storing result
                if(requester.getId() != getId()){
                    getNodeConnectionApi().sendStoreResults(this, requester, key, true);
                }
                break;
            }else {
                if(nodeToIgnore != null && nodeToIgnore.getId() == externalNode.getId()){
                    System.out.println("Should ignore " + nodeToIgnore.getId());
                    continue;
                }
                //otherwise try next close requester in routing table
                PingAnswer pingAnswer = null;
                //if external node is alive, tell it to store the data
                if(externalNode.getLastSeen().before(date) || (pingAnswer = getNodeConnectionApi().ping(this, externalNode)).isAlive()){
                    getNodeConnectionApi().storeAsync(this, requester, externalNode, key, value);
                    storeAnswer = getNewStoreAnswer(key, StoreAnswer.Action.PASSED, requester);
                    break;

                    //otherwise remove the external node from routing table, since its offline
                }else if(pingAnswer != null){
                    getRoutingTable().delete(externalNode);
                }
            }

        }

        return storeAnswer;
    }

    /**
     * Finds external nodes that might contain data
     * @param requester node that requested for data
     * @param key key to look for
     * @param nodeToIgnore nullable. node to ignore when passing requests to others. used when `nodeToIgnore` might actually be closest node but doesnt hold the data
     * @return
     */
    protected GetAnswer<K, V> findClosestNodesToGetData(Node<C> requester, K key, Node<C> nodeToIgnore){
        GetAnswer<K, V> getAnswer = null;
        int hash = boundedHashUtil.hash(key.hashCode());
        FindNodeAnswer<C> findNodeAnswer = getRoutingTable().findClosest(hash);
        Date date = getDateOfSecondsAgo(LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE);
        for (ExternalNode<C> externalNode : findNodeAnswer.getNodes()) {
            //ignore self because we already checked if current node holds the data or not
            if(externalNode.getId() == getId() || (nodeToIgnore != null && externalNode.getId() == nodeToIgnore.getId()))
                continue;

            PingAnswer pingAnswer = null;
            //if node is alive, ask for data
            if(externalNode.getLastSeen().before(date) || (pingAnswer = getNodeConnectionApi().ping(this, externalNode)).isAlive()){
                getNodeConnectionApi().getRequest(this, requester, externalNode, key);
                getAnswer = getNewGetAnswer(key, null, GetAnswer.Action.PASSED, this);
                break;

            //otherwise remove the node from routing table, since its offline
            }else if(pingAnswer != null){
                getRoutingTable().delete(externalNode);
            }
        }

        return getAnswer;
    }


    private GetAnswer<K, V> getNewGetAnswer(K k, V v, GetAnswer.Action action, Node<C> node){
        GetAnswer<K, V> getAnswer = new GetAnswer<>();
        getAnswer.setAction(action);
        getAnswer.setKey(k);
        getAnswer.setValue(v);
        getAnswer.setAlive(true);
        getAnswer.setNodeId(node.getId());
        return getAnswer;
    }

    private StoreAnswer<K> getNewStoreAnswer(K k, StoreAnswer.Action action, Node<C> node){
        StoreAnswer<K> storeAnswer = new StoreAnswer<>();
        storeAnswer.setAlive(true);
        storeAnswer.setNodeId(node.getId());
        storeAnswer.setKey(k);
        storeAnswer.setAction(action);
        return storeAnswer;
    }

}
