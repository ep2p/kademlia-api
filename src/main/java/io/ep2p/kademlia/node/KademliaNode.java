package io.ep2p.kademlia.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.protocol.handler.*;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.protocol.message.*;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.kademlia.util.KadDistanceUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class KademliaNode<ID extends Number, C extends ConnectionInfo> implements KademliaNodeAPI<ID, C> {
    @Getter
    private final ID id;
    @Getter
    private final C connectionInfo;
    @Getter
    private final RoutingTable<ID, C, Bucket<ID, C>> routingTable;
    @Getter
    private final MessageSender<ID, C> messageSender;
    @Getter
    private final NodeSettings nodeSettings;


    //** None Accessible Fields **//
    protected final Map<String, MessageHandler<ID, C>> messageHandlerRegistry = new ConcurrentHashMap<>();
    protected final ExecutorService executorService = Executors.newFixedThreadPool(1);
    protected final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean isRunning;


    public KademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings) {
        this.id = id;
        this.connectionInfo = connectionInfo;
        this.routingTable = routingTable;
        this.messageSender = messageSender;
        this.nodeSettings = nodeSettings;
        this.init();
    }

    @Override
    public void start() {
        pingSchedule();
        this.isRunning = true;
    }

    @Override
    public Future<Boolean> start(Node<ID, C> bootstrapNode) {
        Future<Boolean> booleanFuture = this.bootstrap(bootstrapNode);
        this.start();
        return booleanFuture;
    }

    @Override
    public void stop() {
        this.executorService.shutdownNow();
        this.scheduledExecutorService.shutdownNow();
        this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public KademliaMessage<ID, C, ? extends Serializable> onMessage(KademliaMessage<ID, C, ? extends Serializable> message) throws HandlerNotFoundException {
        assert message != null;
        var messageHandler = messageHandlerRegistry.get(message.getType());
        if (messageHandler == null)
            throw new HandlerNotFoundException(message.getType());
        return messageHandler.handle(this, message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler) {
        this.messageHandlerRegistry.put(type, messageHandler);
    }

    @Override
    public void setLastSeen(Date date) {
        // Nothing to do here
    }


    //***************************//
    //** None-API methods here **//
    //***************************//

    protected final void init(){
        this.registerMessageHandler(PongKademliaMessage.TYPE, new PongMessageHandler<ID, C>());
        this.registerMessageHandler(PingKademliaMessage.TYPE, new PingMessageHandler<>());
        this.registerMessageHandler(FindNodeRequestMessage.TYPE, new FindNodeRequestMessageHandler<>());
        this.registerMessageHandler(FindNodeResponseMessage.TYPE, new FindNodeResponseMessageHandler<>());
    }

    @SneakyThrows
    protected Future<Boolean> bootstrap(Node<ID, C> bootstrapNode){
        final KademliaNodeAPI<ID, C> caller = this;
        this.getRoutingTable().update(bootstrapNode);

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                FindNodeRequestMessage<ID, C> message = new FindNodeRequestMessage<>();
                message.setData(caller.getId());
                try {
                    var response = getMessageSender().sendMessage(caller, bootstrapNode, message);
                    onMessage(response);
                    completableFuture.complete(true);
                } catch (Exception e) {
                    completableFuture.complete(false);
                }
            }
        });

        return completableFuture;
    }

    protected void pingSchedule(){
        final KademliaNodeAPI<ID, C> caller = this;

        this.scheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        List<Node<ID, C>> referencedNodes = new ArrayList<>();

                        List<ID> distances = KadDistanceUtil.getNodesWithDistance(getId(), getNodeSettings().getIdentifierSize());
                        distances.forEach(distance -> {
                            FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(distance);
                            if (findNodeAnswer.getNodes().size() > 0) {
                                if(!findNodeAnswer.getNodes().get(0).getId().equals(getId()) && !referencedNodes.contains(findNodeAnswer.getNodes().get(0)))
                                    referencedNodes.add(findNodeAnswer.getNodes().get(0));
                            }
                        });

                        PingKademliaMessage<ID, C> message = new PingKademliaMessage<>();
                        referencedNodes.forEach(node -> {
                            var response = getMessageSender().sendMessage(caller, node, message);
                            try {
                                onMessage(response);
                            } catch (HandlerNotFoundException e) {
                                // TODO
                            }
                        });
                    }
                },
                0,
                this.getNodeSettings().getPingServiceScheduleTimeValue(),
                this.getNodeSettings().getPingServiceScheduleTimeUnit()
        );
    }
}
