package io.ep2p.kademlia.node;

import com.google.common.base.Objects;
import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.node.strategies.ReferencedNodesStrategy;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.handler.*;
import io.ep2p.kademlia.protocol.message.FindNodeRequestMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.kademlia.protocol.message.ShutdownKademliaMessage;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


@Slf4j
public class KademliaNode<I extends Number, C extends ConnectionInfo> implements KademliaNodeAPI<I, C> {
    @Getter
    private final I id;
    @Getter
    private final C connectionInfo;
    @Getter
    private final RoutingTable<I, C, Bucket<I, C>> routingTable;
    @Getter
    private final transient MessageSender<I, C> messageSender;
    @Getter
    private final transient NodeSettings nodeSettings;

    @Getter
    private final transient ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Getter
    private final transient ScheduledExecutorService scheduledExecutorService;
    @Getter
    @Setter
    private transient ReferencedNodesStrategy referencedNodesStrategy = ReferencedNodesStrategy.Strategies.CLOSEST_PER_BUCKET.getReferencedNodesStrategy();

    //** None Accessible Fields **//
    protected final transient Map<String, MessageHandler<I, C>> messageHandlerRegistry = new ConcurrentHashMap<>();
    private volatile boolean isRunning;


    public KademliaNode(I id, C connectionInfo, RoutingTable<I, C, Bucket<I, C>> routingTable, MessageSender<I, C> messageSender, NodeSettings nodeSettings) {
        this(id, connectionInfo, routingTable, messageSender, nodeSettings, Executors.newSingleThreadScheduledExecutor());
    }

    public KademliaNode(I id, C connectionInfo, RoutingTable<I, C, Bucket<I, C>> routingTable, MessageSender<I, C> messageSender, NodeSettings nodeSettings, ScheduledExecutorService scheduledExecutorService) {
        this.id = id;
        this.connectionInfo = connectionInfo;
        this.routingTable = routingTable;
        this.messageSender = messageSender;
        this.nodeSettings = nodeSettings;
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
    public Future<Boolean> start(Node<I, C> bootstrapNode) {
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
    public KademliaMessage<I, C, ? extends Serializable> onMessage(KademliaMessage<I, C, ? extends Serializable> message) throws HandlerNotFoundException {
        if (message == null) {
            throw new IllegalArgumentException("Message can not be null");
        }
        MessageHandler<I, C> messageHandler = messageHandlerRegistry.get(message.getType());
        if (messageHandler == null)
            throw new HandlerNotFoundException(message.getType());
        return messageHandler.handle(this, message);
    }

    @Override
    public void registerMessageHandler(String type, MessageHandler<I, C> messageHandler) {
        this.messageHandlerRegistry.put(type, messageHandler);
    }

    @Override
    public MessageHandler<I, C> getHandler(String type) throws HandlerNotFoundException {
        MessageHandler<I, C> handler = this.messageHandlerRegistry.get(type);
        if (handler == null){
            throw new HandlerNotFoundException(type);
        }
        return handler;
    }


    //***************************//
    //** None-API methods here **//
    //***************************//

    protected void gracefulShutdown(){
        this.referencedNodesStrategy.getReferencedNodes(this).forEach(node -> getMessageSender().sendAsyncMessage(this, node, new ShutdownKademliaMessage<>()));
    }

    protected void init(){
        this.registerMessageHandler(MessageType.EMPTY, new GeneralResponseMessageHandler<>());
        this.registerMessageHandler(MessageType.PONG, new PongMessageHandler<>());
        this.registerMessageHandler(MessageType.PING, new PingMessageHandler<>());
        this.registerMessageHandler(MessageType.FIND_NODE_REQ, new FindNodeRequestMessageHandler<>());
        this.registerMessageHandler(MessageType.FIND_NODE_RES, new FindNodeResponseMessageHandler<>());
        this.registerMessageHandler(MessageType.SHUTDOWN, new ShutdownMessageHandler<>());
    }


    protected Future<Boolean> bootstrap(Node<I, C> bootstrapNode) {
        this.getRoutingTable().forceUpdate(bootstrapNode);

        KademliaNode<I, C> node = this;
        return this.executorService.submit(() -> {
            FindNodeRequestMessage<I, C> message = new FindNodeRequestMessage<>();
            message.setData(node.getId());
            try {
                KademliaMessage<I, C, ?> response = node.getMessageSender().sendMessage(node, bootstrapNode, message);
                onMessage(response);
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            } finally {
                executorService.shutdown();
            }
        });
    }

    protected void pingSchedule(){
        this.scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    List<Node<I, C>> referencedNodes = this.referencedNodesStrategy.getReferencedNodes(this);

                    PingKademliaMessage<I, C> message = new PingKademliaMessage<>();
                    referencedNodes.forEach(node -> {
                        try {
                            KademliaMessage<I, C, ?> response = getMessageSender().sendMessage(this, node, message);
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

    @Override
    public String toString() {
        return "KademliaNode{" +
                "id=" + id +
                ", connectionInfo=" + connectionInfo +
                '}';
    }
}
