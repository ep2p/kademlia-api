package io.ep2p.kademlia.node;

import com.google.common.base.Objects;
import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.*;
import io.ep2p.kademlia.protocol.message.FindNodeRequestMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.kademlia.protocol.message.ShutdownKademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static io.ep2p.kademlia.util.KadDistanceUtil.getReferencedNodes;

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

    @Getter
    private final ExecutorService executorService;
    @Getter
    private final ScheduledExecutorService scheduledExecutorService;

    //** None Accessible Fields **//
    protected final Map<String, MessageHandler<ID, C>> messageHandlerRegistry = new ConcurrentHashMap<>();
    private volatile boolean isRunning;


    public KademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings) {
        this(id, connectionInfo, routingTable, messageSender, nodeSettings, Executors.newFixedThreadPool(1), Executors.newSingleThreadScheduledExecutor());
    }

    public KademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.connectionInfo = connectionInfo;
        this.routingTable = routingTable;
        this.messageSender = messageSender;
        this.nodeSettings = nodeSettings;
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.init();
    }

    @Override
    public void start() {
        pingSchedule();
        getRoutingTable().forceUpdate(this);
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
        this.gracefulShutdown();
        if (this.isRunning()){
            this.executorService.shutdown();
            this.scheduledExecutorService.shutdown();
            this.isRunning = false;
        }
    }

    @Override
    public void stopNow() {
        if (this.isRunning()){
            this.executorService.shutdownNow();
            this.scheduledExecutorService.shutdownNow();
            this.isRunning = false;
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public KademliaMessage<ID, C, ? extends Serializable> onMessage(KademliaMessage<ID, C, ? extends Serializable> message) throws HandlerNotFoundException {
        assert message != null;
        MessageHandler<ID, C> messageHandler = messageHandlerRegistry.get(message.getType());
        if (messageHandler == null)
            throw new HandlerNotFoundException(message.getType());
        return messageHandler.handle(this, message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<ID, C> messageHandler) {
        this.messageHandlerRegistry.put(type, messageHandler);
    }

    @Override
    public MessageHandler<ID, C> getHandler(String type) throws HandlerNotFoundException {
        MessageHandler<ID, C> handler = this.messageHandlerRegistry.get(type);
        if (handler == null){
            throw new HandlerNotFoundException(type);
        }
        return handler;
    }

    @Override
    public void setLastSeen(Date date) {
        // Nothing to do here
    }


    //***************************//
    //** None-API methods here **//
    //***************************//

    protected void gracefulShutdown(){
        getReferencedNodes(this).forEach(node -> getMessageSender().sendMessage(this, node, new ShutdownKademliaMessage<>()));
    }

    protected void init(){
        this.registerMessageHandler(MessageType.EMPTY, new GeneralResponseMessageHandler<>());
        this.registerMessageHandler(MessageType.PONG, new PongMessageHandler<>());
        this.registerMessageHandler(MessageType.PING, new PingMessageHandler<>());
        this.registerMessageHandler(MessageType.FIND_NODE_REQ, new FindNodeRequestMessageHandler<>());
        this.registerMessageHandler(MessageType.FIND_NODE_RES, new FindNodeResponseMessageHandler<>());
        this.registerMessageHandler(MessageType.SHUTDOWN, new ShutdownMessageHandler<>());
    }


    protected Future<Boolean> bootstrap(Node<ID, C> bootstrapNode) {
        final KademliaNodeAPI<ID, C> caller = this;
        this.getRoutingTable().forceUpdate(bootstrapNode);

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        this.executorService.submit(() -> {
            FindNodeRequestMessage<ID, C> message = new FindNodeRequestMessage<>();
            message.setData(caller.getId());
            try {
                KademliaMessage<ID, C, ?> response = getMessageSender().sendMessage(caller, bootstrapNode, message);
                onMessage(response);
                completableFuture.complete(true);
            } catch (Exception e) {
                completableFuture.complete(false);
                log.error(e.getMessage(), e);
            }
        });

        return completableFuture;
    }

    protected void pingSchedule(){
        final KademliaNodeAPI<ID, C> caller = this;

        this.scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    List<Node<ID, C>> referencedNodes = getReferencedNodes(caller);

                    PingKademliaMessage<ID, C> message = new PingKademliaMessage<>();
                    referencedNodes.forEach(node -> {
                        try {
                            KademliaMessage<ID, C, ?> response = getMessageSender().sendMessage(caller, node, message);
                            onMessage(response);
                        } catch (HandlerNotFoundException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                },
                0,
                this.getNodeSettings().getPingScheduleTimeValue(),
                this.getNodeSettings().getPingScheduleTimeUnit()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KademliaNode<?, ?> that = (KademliaNode<?, ?>) o;
        return Objects.equal(getId(), that.getId()) && Objects.equal(getConnectionInfo(), that.getConnectionInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), getConnectionInfo());
    }
}
