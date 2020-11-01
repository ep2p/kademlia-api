[![](https://jitpack.io/v/ep2p/kademlia-api.svg)](https://jitpack.io/#ep2p/kademlia-api)

# Kademlia Api
Java Kademlia API for Eleuth network.

This API mainly focuses on routing table and peer choosing logic, and moves data persistence & networking to an abstraction layer.
Therefore, its your responsibility to choose how you want your nodes contact each other. There is enough comment on each method on KademliaNode implementations in repository to help you override them to satisfy your needs of p2p network, since this abstract api can be used in many sort of projects.

## Abstraction Layer

### ConnectionInfo
Create your implementation of `com.github.ep2p.kademlia.connection.ConnectionInfo` which can represent connection information for each node to contact.
This can simply be `ip` and `port` for a TCP/UDP connection. Keep your implementation simple and Serializable.

### NodeConnectionApi
Create your implementation of `com.github.ep2p.kademlia.connection.NodeConnectionApi` based on earlier implemented ConnectionInfo. This API is called when a node wants to send requests/data to other nodes.

Note that there are 4 methods which their implementation is not mandatory:
```
default <K, V> void storeAsync(Node<C> caller, Node<C> requester,  Node<C> node, K key, V value){} //Make sure its async
default <K> void getRequest(Node<C> caller, Node<C> requester, Node<C> node, K key){}
default <K, V> void sendGetResults(Node<C> caller, Node<C> requester, K key, V value){}
default <K> void sendStoreResults(Node<C> caller, Node<C> requester, K key, boolean success){}
```

Normally this Kademlia abstraction doesn't care about storing data. It only joins peers in the network. If you want to create something like DHT and store data, you need to implement these methods too.

### NodeApi
If you call any Node Api methods on one end through `NodeConnectionApi`. on the other end you need to receive them and call appropriate method on you KademliaNode.
Make sure your protocol supports requests for methods available in `com.github.ep2p.kademlia.connection.NodeApi` and also `com.github.ep2p.kademlia.connection.StorageNodeApi` if you are storing data as well.

To make it more clear, you implement **NodeConnectionApi** for requests that are leaving the node, and **NodeApi** (or **StorageNodeApi**) for incoming requests to your node. A big advantage is that you can control the incoming requests, sign them, validate them, or anything you want to do with them before you pass them to Kademlia node.

### RoutingTable
Notice that `RoutingTable` and its buckets are Serializable. So you will easily be able to write it to a file. When you are creating and instance of your node, you can pass a new routing table or use one from disk.
At this stage, there is no helper class for writing routing table on disk.

## Kademlia Node Usage

Currently there are 3 classes that can be used to create a kademlia node.

To create a KademliaNode **without capability of storing data**, try:

```
KademliaNode<?> node = new KademliaNode<>(yourNodeId, routingTable, nodeConnectionApi, connectionInfoImplForThisNode);
```

And to create a KademliaNode **capable of storing data**, try:

```
KademliaRepositoryNode<?, Integer, String> aNode = new KademliaRepositoryNode<>(yourNodeId, routingTable, nodeConnectionApi, connectionInfoImplForThisNode, repository);
```
or
```
KademliaSyncRepositoryNode<?, Integer, String> aNode = new KademliaSyncRepositoryNode<>(yourNodeId, routingTable, nodeConnectionApi, connectionInfoImplForThisNode, repository);
```
Where `Integer` is data storage key type, `String` is data storage value type, and `repository` is data storage implementation. It's a good practice to keep storage outside ram and avoid data loss on node shutdowns, but the choice is yours.

Difference between `KademliaRepositoryNode` and `KademliaSyncRepositoryNode` is that second one waits for `store()` and `get()` to reply, while first one returns an answer even if store or get requests are forwarded to other nodes.

After creating a node instance for first time, you need to bootstrap it using another node you know in network:
```
node.bootstrap(bootstrapNode);
```

If your node is already bootstrapped, just start it:
```
node.start();
``` 

Also you can listen to some events that happen in node, such as start, shutdown, data store and lookup results (specially if u are using `KademliaRepositoryNode`) by setting your listener on the node.
```
node.setKademliaNodeListener(...)
```
Once you get used to API, this listener can be a big help in making some changes to your nodes behaviour. See next section, Re-Distribution.

## Re-Distribution

There are situations where best node to hold a data is not available on network, So closest node will be chosen to keep the data. After certain time, a node with closer id to `key hash` might appear and it would be good practice to pass the data to the new node since it has closer id.

Or there are situations where a node is shutting down and it's good practice for this node to pass all its data to other nodes.

Of course one important parameter here is size of values as it will take much longer to pass large values to other nodes and you must prevent application shutdown till this re-distribution ends.

For these cases, you can use or extend `com.github.ep2p.kademlia.node.RedistributionKademliaNodeListener` as your listener. It will handle data redistribution when new nodes appear. Also by passing `ShutdownDistributionListener` it handles data redistribution when node is shutting down.

---

## Installation

Use jitpack to add library to your project. Maven example:

Add jitpack repository
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add java-kademlia-api repository
```
<dependency>
    <groupId>com.github.ep2p</groupId>
    <artifactId>kademlia-api</artifactId>
    <version>1.3.0-RELEASE</version>
</dependency>
```

## Acknowledgments

Since this project is completely experimental (at least for now), I'd like to thank to some people around the internet that helped me gain some knowledge about Kademlia and its implementations beyond the paper.
I know my implementation as a sample and abstract kademlia api, not most complete.

- A huge thank to  **Jakob Jenkov** for his great [tutorial](http://tutorials.jenkov.com/p2p/index.html) on p2p network and kademlia.
- Stackoverflow Community
    - **Joshua Kissoon** for his great explanation on [Adding new nodes to Kademlia, building Kademlia routing tables](https://stackoverflow.com/a/22740578/5197662)
    - **Nawras** for asking [What exactly K-Bucket means in Kademlia DHT?](https://stackoverflow.com/q/54341261/5197662)
- **Kasra Faghihi** ([@offbynull](https://github.com/offbynull)) for his very mathematical implementation of Kademlia.
- And a great shout out to **SimGrid** Team, for keeping history of [their work on Kademlia](https://gitlab.inria.fr/simgrid/simgrid/-/tree/ce2e676ad127f782b9c959499ab1c042195e411a/examples/java/kademlia) which this repository is inspired by it to implement RoutingTables, Buckets and some more stuff. 

## Videos

### Kademlia Overview 1

In first video of EP2P lecture about Kademlia Algorithm, we are going to gain brief understanding about elements of Kademlia nodes, network structure and Distributed Hash Tables (DHT) in theory.


[![Kademlia Algorithm Overview 1](https://github.com/ep2p/kademlia-api/blob/main/kademlia-ov1.png?raw=1)](https://youtu.be/7o0pfKDq9KE)
