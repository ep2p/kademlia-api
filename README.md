# Eleuth - Java Kademlia Api
Java Kademlia API for Eleuth network.

This API mainly focuses on routing table and peer choosing logic, and moves data persistence & networking to an abstraction layer.
Therefore, its your responsibility to choose how you want your nodes contact each other.

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
Make sure your protocol supports requests for methods available in `com.github.ep2p.kademlia.connection.NodeApi` and also `com.github.ep2p.kademlia.connection.P2PStorageApi` if you are storing data as well.

### RoutingTableFactory
Create your implementation of `com.github.ep2p.kademlia.table.RoutingTableFactory` which can return a `new RoutingTable` based on input id, or read an `old RoutingTable` from disk and return that one.
Notice that `RoutingTable` and its buckets are Serializable. So you will easily be able to write it to a file.

## Kademlia Node Usage

Currently there are 3 classes that can be used to create a kademlia node.

To create a KademliaNode **without capability of storing data**, try:

```
KademliaNode<?> node = new KademliaNode<>(yourNodeId, routingTableFactory, nodeConnectionApi, connectionInfoImplForThisNode);
```

And to create a KademliaNode **capable of storing data**, try:

```
KademliaRepositoryNode<?, Integer, String> aNode = new KademliaRepositoryNode<>(yourNodeId, routingTableFactory, nodeConnectionApi, connectionInfoImplForThisNode, repository);
```
or
```
KademliaSyncRepositoryNode<?, Integer, String> aNode = new KademliaSyncRepositoryNode<>(yourNodeId, routingTableFactory, nodeConnectionApi, connectionInfoImplForThisNode, repository);
```
Where `Integer` is data storage key type, `String` is data storage value type, and `repository` is data storage implementation. It's a good practice to keep storage outside ram and avoid data loss on node shutdowns, but the choice is yours.

Difference between `KademliaRepositoryNode` and `KademliaSyncRepositoryNode` is that second one waits for `store()` and `get()` to reply, while first one returns an answer even if store or get requests are forwarded to other nodes.

---

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
    <artifactId>eleuth-java-kademlia-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Acknowledgments

Since this project is completely experimental (at least for now), I'd like to thank to some people around the internet that helped me gain some knowledge about Kademlia and its implementations beyond the paper.
I know my implementation as a sample. nothing more.

- A huge thank to  **Jakob Jenkov** for his great [tutorial](http://tutorials.jenkov.com/p2p/index.html) on p2p network and kademlia.
- Stackoverflow Community
    - **Joshua Kissoon** for his great explanation on [Adding new nodes to Kademlia, building Kademlia routing tables](https://stackoverflow.com/a/22740578/5197662)
    - **Nawras** for asking [What exactly K-Bucket means in Kademlia DHT?](https://stackoverflow.com/q/54341261/5197662)
- **Kasra Faghihi** ([@offbynull](https://github.com/offbynull)) for his very mathematical implementation of Kademlia.
- And a great shout out to **SimGrid** Team, for keeping history of [their work on Kademlia](https://gitlab.inria.fr/simgrid/simgrid/-/tree/ce2e676ad127f782b9c959499ab1c042195e411a/examples/java/kademlia) which this repository is inspired by it to implement RoutingTables, Buckets and some more stuff. 