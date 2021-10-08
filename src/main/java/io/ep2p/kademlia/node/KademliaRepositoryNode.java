package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.kademlia.connection.StorageNodeApi;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.ShutdownException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.PingAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.service.RepublishStrategy;
import io.ep2p.kademlia.service.RepublishStrategyFactory;
import io.ep2p.kademlia.v4.table.Bucket;
import io.ep2p.kademlia.v4.table.RoutingTable;
import io.ep2p.kademlia.util.DateUtil;
import io.ep2p.kademlia.util.KeyHashGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * KademliaNode child which implements StorageNodeApi (async)
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 * @param <K> storage key type
 * @param <V> storage value type
 */
@Slf4j
public class KademliaRepositoryNode<ID extends Number, C extends ConnectionInfo, K, V> extends KademliaNode<ID, C> implements StorageNodeApi<ID, C, K, V> {
    @Getter
    private KademliaRepository<K,V> kademliaRepository;
    @Setter
    @Getter
    private KeyHashGenerator<ID, K> keyHashGenerator;
    @Getter
    protected RepublishStrategy<ID, C, K, V> republishStrategy;


    public KademliaRepositoryNode(ID nodeId, RoutingTable<ID, C, Bucket<ID, C>> routingTable, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        super(nodeId, routingTable, nodeConnectionApi, connectionInfo, nodeSettings);
        this.init(kademliaRepository, keyHashGenerator);
    }

    public KademliaRepositoryNode(ID nodeId, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        super(nodeId, nodeConnectionApi, connectionInfo, nodeSettings);
        this.init(kademliaRepository, keyHashGenerator);
    }

    private void init(KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator){
        this.kademliaRepository = kademliaRepository;
        this.keyHashGenerator = keyHashGenerator;
        this.initRepublishStrategy();
    }

    public KademliaRepositoryNode(ID nodeId, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository, KeyHashGenerator<ID, K> keyHashGenerator) {
        this(nodeId, nodeConnectionApi, connectionInfo, NodeSettings.Default.build(), kademliaRepository, keyHashGenerator);
    }

    @SuppressWarnings("unchecked")
    public KademliaRepositoryNode(ID nodeId, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, KademliaRepository<K, V> kademliaRepository) {
        this(nodeId, nodeConnectionApi, connectionInfo, kademliaRepository, new KeyHashGenerator.Default<>((Class<ID>) nodeId.getClass()));
    }

    @SuppressWarnings("unchecked")
    public KademliaRepositoryNode(ID nodeId, RoutingTable<ID, C, Bucket<ID, C>> routingTable, NodeConnectionApi<ID, C> nodeConnectionApi, C connectionInfo, NodeSettings nodeSettings, KademliaRepository<K, V> kademliaRepository) {
        this(nodeId, routingTable, nodeConnectionApi, connectionInfo, nodeSettings, kademliaRepository, new KeyHashGenerator.Default<>((Class<ID>) nodeId.getClass(), nodeSettings));
    }

    protected void initRepublishStrategy(){
        if (this.getNodeSettings().isEnabledRepublishing()){
            if (this.kademliaRepository instanceof TimestampAwareKademliaRepository){
                this.republishStrategy = RepublishStrategyFactory.getRepublishStrategy();
                this.republishStrategy.configure(
                    this,
                    this.getNodeSettings().getRepublishSettings(),
                    (TimestampAwareKademliaRepository<K, V>) this.getKademliaRepository()
                );
            }else {
                log.error("Can not initialize republish-strategy since kademliaRepository is not instance of TimestampAwareKademliaRepository");
            }
        }
    }

    @Override
    public void stop() throws ShutdownException {
        super.stop();
        if (this.republishStrategy != null){
            this.republishStrategy.stop();
        }
    }

    @Override
    public void start() {
        super.start();
        if (this.republishStrategy != null){
            this.republishStrategy.start();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public KademliaNodeListener<ID, C, K, V> getKademliaNodeListener() {
        return (KademliaNodeListener<ID, C, K, V>) super.getKademliaNodeListener();
    }

    /* Node Connection API */

    /**
     * Handlers incoming `Get` requests from other nodes
     * @param caller Node that passed info to current node
     * @param requester Node that looks for info
     * @param key Key of data
     */
    @Override
    public void onGetRequest(Node<ID, C> caller, Node<ID, C> requester, K key){
        //Check repository for key, if it exists return it
        if(kademliaRepository.contains(key)){
            V value = kademliaRepository.get(key);
            getNodeConnectionApi().sendGetResults(this, requester, key, value);
            return;
        }

        GetAnswer<ID, K, V> getAnswer = getDataFromClosestNodes(requester, key, caller);
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
    public void onStoreRequest(Node<ID, C> caller, Node<ID, C> requester, K key, V value) {
        try {
            StoreAnswer<ID, K> storeAnswer = handleStore(key, value, false, requester, caller);
            if (storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
                getNodeConnectionApi().sendStoreResults(this, requester, key, false);
            }else if(storeAnswer.getResult().equals(StoreAnswer.Result.STORED)) {
                getNodeConnectionApi().sendStoreResults(this, requester, key, true);
            }
        } catch (StoreException ignore) {}
    }

    /* Managing API */

    /**
     * Store data on closest node, if none was found, check force store
     * @param key Key to store
     * @param value Value to store
     * @param force determines if value should be persisted if no close nodes is found
     * @return Storing result "STORED: when current node stores data" , "PASSED: When storing request is passed to other nodes"
     * @throws StoreException thrown when no responsible node was found
     */
    public StoreAnswer<ID, K> store(K key, V value, boolean force) throws StoreException {
        return handleStore(key, value, force, this, null);
    }

    /**
     * The method with the top level storing logic.
     * Its better to make changes to this method to override how data persists
     * @param key Key to store
     * @param value Value to store
     * @param force determines if value should be persisted if no close nodes is found
     * @return Storing result "STORED: when current node stores data" , "PASSED: When storing request is passed to other nodes"
     * @throws StoreException thrown when no responsible node was found
     */
    protected StoreAnswer<ID, K> handleStore(K key, V value, boolean force, Node<ID, C> requester, Node<ID, C> caller) throws StoreException {
        if(!isRunning())
            throw new StoreException("Node is not running");
        StoreAnswer<ID, K> storeAnswer = null;
        ID hash = hash(key);

        // If some other node is calling the store, and that other node is not this node, but the origin request is by this node, then persist it
        if (caller != null && !caller.getId().equals(getId()) && requester.getId().equals(getId())){
            return doStore(key, value);
        }

        //if current node should persist data, do it immediately
        if(getId().equals(hash)) {
            storeAnswer = doStore(key, value);
        }else {
            FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(hash);
            storeAnswer = storeDataToClosestNode(requester, findNodeAnswer.getNodes(), key, value, caller);
        }

        if(storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)){
            if (force){
                storeAnswer = doStore(key, value);
            }
        }
        return storeAnswer;
    }


    private StoreAnswer<ID, K> doStore(K key, V value){
        kademliaRepository.store(key, value);
        return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
    }

    /**
     * @param key Key for data to look
     * @return GetAnswer, "Found: current node holds data and returned it", "PASSED: Current node doesnt hold data so request is passed to other nodes"
     * @throws GetException No responsible node found for key
     */
    public GetAnswer<ID, K, V> get(K key) throws GetException {
        if(!isRunning())
            throw new GetException("Node is not running");
        //Check repository for key, if it exists return it
        if(kademliaRepository.contains(key)){
            V value = kademliaRepository.get(key);
            return getNewGetAnswer(key, value, GetAnswer.Result.FOUND, this);
        }

        GetAnswer<ID, K, V> getAnswer = null;

        //Otherwise, ask closest node we know to key
        getAnswer = getDataFromClosestNodes(this, key, null);

        if(getAnswer == null){
            getAnswer = new GetAnswer<>();
            getAnswer.setKey(key);
            getAnswer.setResult(GetAnswer.Result.FAILED);
            getAnswer.setNodeId(this.getId());
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
    public void onGetResult(Node<ID, C> node, K key, V value) {
        getKademliaNodeListener().onKeyLookupResult(this, node, key, value);
    }

    /**
     * Called when a key value is stored in network
     * @param node Node that holds key
     * @param key Key itself
     */
    @Override
    public void onStoreResult(Node<ID, C> node, K key, boolean successful) {
        getKademliaNodeListener().onKeyStoredResult(this, node, key, successful);
    }


    /* --- */

    protected StoreAnswer<ID, K> storeDataToClosestNode(Node<ID, C> requester, List<ExternalNode<ID, C>> externalNodeList, K key, V value, Node<ID, C> nodeToIgnore){
        Date date = DateUtil.getDateOfSecondsAgo(this.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        StoreAnswer<ID, K> storeAnswer = null;
        for (ExternalNode<ID, C> externalNode : externalNodeList) {
            //if current node is closest node, store the value
            if(externalNode.getId().equals(getId())){
                kademliaRepository.store(key, value);
                storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
                break;
            }else {
                if (nodeToIgnore != null && nodeToIgnore.getId().equals(externalNode.getId())){
                    continue;
                }
                if (requester.getId().equals(externalNode.getId())){
                    continue;
                }
                //otherwise try next close requester in routing table
                PingAnswer<ID> pingAnswer = null;
                //if external node is alive, tell it to store the data
                if(externalNode.getLastSeen().before(date) || (pingAnswer = getNodeConnectionApi().ping(this, externalNode)).isAlive()){
                    getNodeConnectionApi().storeAsync(this, requester, externalNode, key, value);
                    storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.PASSED, requester);
                    break;

                    //otherwise remove the external node from routing table, since its offline
                }else if(!pingAnswer.isAlive()){
                    getRoutingTable().delete(externalNode);
                }
            }

        }
        if (storeAnswer == null)
            storeAnswer = getNewStoreAnswer(key, StoreAnswer.Result.FAILED, requester);
        return storeAnswer;
    }

    /**
     * Finds external nodes that might contain data
     * @param requester node that requested for data
     * @param key key to look for
     * @param nodeToIgnore nullable. node to ignore when passing requests to others. used when `nodeToIgnore` might actually be closest node but doesnt hold the data
     * @return GetAnswer
     */
    protected GetAnswer<ID, K, V> getDataFromClosestNodes(Node<ID, C> requester, K key, Node<ID, C> nodeToIgnore){
        GetAnswer<ID, K, V> getAnswer = null;
        ID hash = hash(key);
        FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(hash);
        Date date = DateUtil.getDateOfSecondsAgo(this.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : findNodeAnswer.getNodes()) {
            //ignore self because we already checked if current node holds the data or not
            //Also ignore nodeToIgnore if its not null
            if(externalNode.getId().equals(getId()) || (nodeToIgnore != null && externalNode.getId().equals(nodeToIgnore.getId())))
                continue;

            PingAnswer<ID> pingAnswer = null;
            //if node is alive, ask for data
            if(externalNode.getLastSeen().before(date) || (pingAnswer = getNodeConnectionApi().ping(this, externalNode)).isAlive()){
                getNodeConnectionApi().getRequest(this, requester, externalNode, key);
                getAnswer = getNewGetAnswer(key, null, GetAnswer.Result.PASSED, this);
                break;

            //otherwise remove the node from routing table, since its offline
            }else if(!pingAnswer.isAlive()){
                getRoutingTable().delete(externalNode);
            }
        }

        return getAnswer;
    }

    protected ID hash(K key){
        return this.keyHashGenerator.generate(key);
    }

    protected GetAnswer<ID, K, V> getNewGetAnswer(K k, V v, GetAnswer.Result result, Node<ID, C> node){
        GetAnswer<ID, K, V> getAnswer = new GetAnswer<>();
        getAnswer.setResult(result);
        getAnswer.setKey(k);
        getAnswer.setValue(v);
        getAnswer.setAlive(true);
        getAnswer.setNodeId(node.getId());
        return getAnswer;
    }

    protected StoreAnswer<ID, K> getNewStoreAnswer(K k, StoreAnswer.Result result, Node<ID, C> node){
        StoreAnswer<ID, K> storeAnswer = new StoreAnswer<>();
        storeAnswer.setAlive(true);
        storeAnswer.setNodeId(node.getId());
        storeAnswer.setKey(k);
        storeAnswer.setResult(result);
        return storeAnswer;
    }

}
