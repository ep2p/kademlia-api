package io.ep2p.kademlia.v4.node;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.util.KadDistanceUtil;
import io.ep2p.kademlia.v4.connection.ConnectionInfo;
import io.ep2p.kademlia.v4.connection.MessageSender;
import io.ep2p.kademlia.v4.exception.HandlerNotFoundException;
import io.ep2p.kademlia.v4.message.FindNodeRequestMessage;
import io.ep2p.kademlia.v4.message.KademliaMessage;
import io.ep2p.kademlia.v4.message.handler.MessageHandler;
import io.ep2p.kademlia.v4.table.Bucket;
import io.ep2p.kademlia.v4.table.RoutingTable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
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


    public KademliaNode(ID id, C connectionInfo, RoutingTable<ID, C, Bucket<ID, C>> routingTable, MessageSender<ID, C> messageSender, NodeSettings nodeSettings) {
        this.id = id;
        this.connectionInfo = connectionInfo;
        this.routingTable = routingTable;
        this.messageSender = messageSender;
        this.nodeSettings = nodeSettings;
    }

    @Override
    public void start() {
        this.init();
        pingSchedule();
    }

    @Override
    public void start(Node<ID, C> bootstrapNode) {
        this.bootstrap(bootstrapNode);
        this.start();
    }

    @Override
    public void stop() {
        this.executorService.shutdownNow();
        this.scheduledExecutorService.shutdownNow();
    }

    @Override
    public <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, O> onMessage(KademliaMessage<ID, C, I> message) throws HandlerNotFoundException {
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
    public void setLastSeen(Date date) {
        // Nothing to do here
    }


    //***************************//
    //** None-API methods here **//
    //***************************//

    //todo: register message listeners here
    protected final void init(){

    }

    @SneakyThrows
    protected void bootstrap(Node<ID, C> bootstrapNode){
        final KademliaNodeAPI<ID, C> caller = this;
        this.getRoutingTable().update(bootstrapNode);
        this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                FindNodeRequestMessage<ID, C> message = new FindNodeRequestMessage<>();
                message.setData(caller.getId());
                getMessageSender().sendMessage(caller, bootstrapNode, message);
            }
        });
    }

    protected void pingSchedule(){
        final KademliaNodeAPI<ID, C> caller = this;
        this.scheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        List<Node<ID, C>> referencedNodes = new CopyOnWriteArrayList<>();

                        List<ID> distances = KadDistanceUtil.getNodesWithDistance(getId(), getNodeSettings().getIdentifierSize());
                        distances.forEach(distance -> {
                            FindNodeAnswer<ID, C> findNodeAnswer = getRoutingTable().findClosest(distance);
                            if (findNodeAnswer.getNodes().size() > 0) {
                                if(!findNodeAnswer.getNodes().get(0).getId().equals(getId()) && !referencedNodes.contains(findNodeAnswer.getNodes().get(0)))
                                    referencedNodes.add(findNodeAnswer.getNodes().get(0));
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
