package io.ep2p.kademlia.service;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.*;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.table.Bucket;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * This class uses ScheduledExecutorService to create a schedule for republishing keys of certain age
 * The age of the keys and schedule configuration are available in RepublishSettings
 *
 * To republish keys, this class first attaches a listener to the kademlia node so it will be informed if the republished key was successfully persisted
 * To choose which nodes to republish the message to, it chooses the closest node to itself and then a random node from each bucket
 *
 * @param <ID> type of node ID
 * @param <C> type of node ConnectionInfo
 * @param <K> type of node repository Key
 * @param <V> type of node repository Value
 */
@Slf4j
public class DefaultRepublishStrategy<ID extends Number, C extends ConnectionInfo, K, V> implements RepublishStrategy<ID, C, K, V>, Runnable {
    private KademliaRepositoryNode<ID, C, K, V>  kademliaRepositoryNode;
    private NodeSettings.RepublishSettings republishSettings;
    private TimestampAwareKademliaRepository<K, V> timestampAwareKademliaRepository;
    private ScheduledExecutorService scheduledExecutorService = null;
    private volatile boolean isRunning = false;

    @Override
    public void configure(
            KademliaRepositoryNode<ID, C, K, V> kademliaRepositoryNode,
            NodeSettings.RepublishSettings republishSettings,
            TimestampAwareKademliaRepository<K, V> timestampAwareKademliaRepository
    ){
        this.republishSettings = republishSettings;
        this.kademliaRepositoryNode = kademliaRepositoryNode;
        this.timestampAwareKademliaRepository = timestampAwareKademliaRepository;
        try {
            this.init();
        } catch (InterruptedException e) {
            log.error("Failed to init DefaultRepublishStrategy", e);
        }
    }

    public synchronized void init() throws InterruptedException {
        if (this.scheduledExecutorService != null){
            this.scheduledExecutorService.shutdown();
            if(this.scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES)){
                this.scheduledExecutorService.shutdownNow();
            }
            this.scheduledExecutorService = null;
        }
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.isRunning = false;

        // Register listener to remove key from repository if republish was successful
        // If the listener is already decorated, skip it (second if)
        if (!(
                this.kademliaRepositoryNode.getKademliaNodeListener() instanceof RepublishKademliaNodeListenerDecorator
        )){
            this.kademliaRepositoryNode.setKademliaNodeListener(
                    new RepublishKademliaNodeListenerDecorator<>(this.kademliaRepositoryNode.getKademliaNodeListener())
            );
        }
    }

    @Override
    public synchronized void start() {
        if (this.isRunning){
            return;
        }

        this.scheduledExecutorService.scheduleAtFixedRate(
                this,
                0,
                this.republishSettings.getRepublishIntervalValue(),
                this.republishSettings.getRepublishIntervalUnit());
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.scheduledExecutorService.shutdownNow();
    }

    @Override
    public void run() {
        Map<K, V> result = null;
        while ((
                result = this.timestampAwareKademliaRepository.getDataOlderThan(
                        this.republishSettings.getRepublishQueryTimeValue(),
                        this.republishSettings.getRepublishQueryUnit(),
                        this.republishSettings.getRepublishQuerySize()
                )
        ).size() > 0){
            result.forEach(this::handleKeyRepublish);
        }
    }

    public void handleKeyRepublish(K key, V value){
        for(Node<ID, C> node : getNodesToPublishTo(key)){
            this.kademliaRepositoryNode.getNodeConnectionApi().storeAsync(
                    this.kademliaRepositoryNode, this.kademliaRepositoryNode, node, key, value
            );
        }
    }

    public List<Node<ID, C>> getNodesToPublishTo(K key){
        List<Node<ID, C>> results = new ArrayList<>();

        // Add closest node first
        FindNodeAnswer<ID, C> findNodeAnswer = this.kademliaRepositoryNode.getRoutingTable().findClosest(
                this.kademliaRepositoryNode.getKeyHashGenerator().generate(key)
        );

        for (ExternalNode<ID, C> node : findNodeAnswer.getNodes()) {
            if (node.getId().equals(this.kademliaRepositoryNode.getId())){
                continue;
            }
            results.add(node);
            break;
        }

        return results;
    }


    public static class RepublishKademliaNodeListenerDecorator<ID extends Number, C extends ConnectionInfo, K, V> extends KademliaNodeListenerDecorator<ID, C, K, V>{

        public RepublishKademliaNodeListenerDecorator(KademliaNodeListener<ID, C, K, V> kademliaNodeListener) {
            super(kademliaNodeListener);
        }

        @Override
        public void onKeyStoredResult(KademliaNode<ID, C> kademliaNode, Node<ID, C> node, K key, boolean success) {
            super.onKeyStoredResult(kademliaNode, node, key, success);
            KademliaRepositoryNode<ID, C, K, V> repositoryNode = (KademliaRepositoryNode<ID, C, K, V>) kademliaNode;
            if (success && !node.getId().equals(kademliaNode.getId()) && repositoryNode.getKademliaRepository().contains(key)){
                repositoryNode.getKademliaRepository().remove(key);
            }
        }
    }
}
