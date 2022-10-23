[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.ep2p/kademlia-api/badge.png?gav=true)](https://maven-badges.herokuapp.com/maven-central/io.ep2p/kademlia-api)
[![Github Releases](https://badgen.net/github/release/ep2p/kademlia-api)](https://github.com/ep2p/kademlia-api/releases)
[![Open Issues](https://badgen.net/github/open-issues/ep2p/kademlia-api)](https://github.com/ep2p/kademlia-api/issues)
[![Liscence](https://badgen.net/github/license/ep2p/kademlia-api)](https://github.com/ep2p/kademlia-api/blob/main/LICENSE)

# Kademlia Api
Abstract Java Kademlia API

This API mainly focuses on routing table, peer choosing logic and DHT basics, and moves data persistence & networking to an abstraction layer.

Networking layer which covers the subjects of how packets move through the network, serialization and validation of packets should be implemented by the developer and your protocol of choice. 


## Key Features

- Allows peer GUID to be an `Integer`, `Long` or even `BigInteger`
- Automatically handles most of the operations related to GUID type
- Highly configurable
  - There is an abstraction for most important things such as DHT repository or networking layer (where messages are sent and received)
  - Other configurations can be passed through `NodeSettings` where the property names are clean and informative
  - Protocol can easily change by overriding the default `MessageHandlers`
  - There are many available `decorator`s so API is easily extendable
- It's written protocol based where all messages have `type`s, and you can register different handler for each message type.
- Ping, Pong, Find node (+ bootstrapping), DHT (store & lookup) are already implemented

---

## Installation

Using **maven central**

```xml
<dependency>
    <groupId>io.ep2p</groupId>
    <artifactId>kademlia-api</artifactId>
    <version>4.1.8-RELEASE</version>
</dependency>
```

Github releases page only contains certain releases. See [all maven releases](https://search.maven.org/artifact/io.ep2p/kademlia-api) in maven repository and choose the newest one there.

**Important**: Since version 4 many things has changed, so we recommend you to avoid using any older versions than 4.0.0.

---

## Abstraction Layer

### ConnectionInfo
Create your implementation of `com.github.ep2p.kademlia.connection.ConnectionInfo` which can represent connection information for each node to contact.
This can simply be `ip` and `port` for a TCP/UDP connection. Keep your implementation simple and Serializable.


```
.=====================================================.             .=====================================================.
|                                    .----------------|             |                                                     |
|                                    | Message Sender |----         |-----------------------------------------------------|
|                                    '----------------|    \        |----------------.                   |                |
|-----------------------------------------------------|     \------>|  onMessage()   |   KADEMLIA NODE   |  Routing Table |
|                |                   .----------------|             |----------------'                   |                |
| Routing Table  |   KADEMLIA NODE   |    Node API    |<------      |-----------------------------------------------------|
|                |                   '----------------|       \     |----------------.                                    |
|-----------------------------------------------------|        \----| Message Sender |                                    |
|                                                     |             |----------------'                                    |
'====================================================='             '====================================================='

                                                         Figure 1
```


### MessageSender Interface

This is the networking layer. Should be implemented to send message (`KademliaMessage`) from the `caller` kademlia-node to `receiver` node.
Serialization, Validation, and request sending are the basic things that shall be done here. 
The output of the API call probably has a response which should still be converted to the proper `KademliaMessage` and returned to the caller.

On the receiver side, when a message comes in, it should be passed to `KademliaNodeAPI.onMessage(message)` after deserialization and validation.

The default package where messages exist is `io.ep2p.kademlia.protocol.message`.
Each message has certain type of serializable data attached to it, and it includes repliers' `id` and `alive` status of replier.

You should pass certain implementation of `KademliaMessage` per each `status` to the node.
Here are the ones that shall be supported and converted during the serialization process.

| Type              | KademliaMessage Class          |
|-------------------|--------------------------------|
| EMPTY             | EmptyKademliaMessage           |
| PING              | PingKademliaMessage            |
| PONG              | PongKademliaMessage            |
| FIND_NODE_REQ     | FindNodeRequestMessage         |
| FIND_NODE_RES     | FindNodeResponseMessage        |
| SHUTDOWN          | ShutdownKademliaMessage        |
| DHT_LOOKUP        | DHTLookupKademliaMessage       |
| DHT_LOOKUP_RESULT | DHTLookupResultKademliaMessage |
| DHT_STORE         | DHTStoreKademliaMessage        |
| DHT_STORE_RESULT  | DHTStoreResultKademliaMessage  |
|                   |                                |


### RoutingTable
Notice that `RoutingTable` and its buckets are Serializable. So you will easily be able to write it to a file. When you are creating an instance of your node, you can pass a new routing table or use one from disk.
At this stage, there is no helper class for writing routing table on disk.

---

## Using Kademlia

For only network bootstrapping and continues routing-table updates you can consider using default `KademliaNodeAPI (interface)` implementation: `KademliaNode (impelementation)`.

```java
KademliaNodeAPI<ID, C> node = new KademliaNode<>(id, connectionionInfo, routingTable, messageSenderAPI, nodeSettings);
```

You can start the node by calling `start()` method, or you can bootstrap it by calling `bootstrap(bootstrapNode)` method and pass `Node<ID, C>` of bootstrap node as the parameter. 

You can stop the node by calling `stop()` method which should gracefully stop the node and its executor services, or you can stop it by calling `stopNow()` method which will immediately the running jobs.

### Generics
Note that KademliaNode and all its subclasses are Generic. 

The first generic type is for ID of the node, and you can choose between `Integer`, `Long`, `BigInteger` as these are the only ones supported and you should decide on which on to use based on your GUID space size. For example on **Eleuth Node System** Biginteger is being used since node IDs are `SHA1` of a public keys. You can [choose the appropriate `RoutingTable`](https://github.com/ep2p/kademlia-api/tree/main/src/main/java/io/ep2p/kademlia/table) implementation based on this key size. There is an implementation available for all of the three supported ID types.

The second generic type is type of your class that implements `ConnectionInfo`.

### Configuration

You can configure your node by passing an instance of `NodeSettings` to `KademliaNode` or it's subclasses. 
By default, they use the configuration from `NodeSettings.Default` which has static fields. You can edit `NodeSettings.Default` if all of the nodes that you want to have in a single application are having the same configuration.


## DHT

For **Distributed Hash Table** you should consider using `DHTKademliaNodeAPI` interface.

```java
DHTKademliaNodeAPI<ID, C, K, V> node = new DHTKademliaNode<>(id, c, routingTable, messageSenderAPI, nodeSettings, repository, keyHashGenerator);
```

Where repository is an implementation of `KademliaRepository<K,V>` to store <Key , Value> data.


One of the most important aspects of a DHT is how we use the hash for keys to make them unique or identifiable. Usually the key is the hash of the value itself. 
However, in this kademlia implementation your hash types should be the same number format as your GUID (`Integer`, `Long`, `BigInteger`).
That's where you'd need a mutual mechanism in all of your peers to generate hash of the key (which it can still be the key itself). You should pass your implementation of `KeyHashGenerator` to `DHTKademliaNode`

---

## Republishing Keys

In DHT we often require to republish keys. For example in a scenario where a node is shutting down there should be a mechanism for the node to republish its keys (and values) to other nodes before shutting down.
This way there won't be much data loss. Another scenario is when a new node joins the network, and we (as an individual node) may want to pass the data that the new node should hold to them.
Therefore, we should periodically republish our data so closer nodes hold it.

This kademlia-api does not implement key republishing since `4.0.0-RELEASE` version. This mechanism can be very tricky and very depended on how each developer implements their `KademliaRepository`.
For example, when a single key can hold large amount of data we'd probably want to find a closer node first and then pass the data to them in different chunks.

---

## Donations

Coffee has a cost :smile:

Any  sort of small or large donations can be a motivation in maintaining this repository and related repositories.

- **ETH**: `0x5F120228C12e2C6923AfDeb0e811d74160166d90`
- **TRC20**: `TJjw5n26KFBqkJQbs7eKdxkVuk4pvJdFzE`
- **BTC**: `bc1qmtewrl7srjrkl8t4z5vantuqkz086srj4clzh3`


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

[![](https://img.shields.io/badge/youtube-Kademlia%20Overview%201-FFF?style=for-the-badge&logo=youtube&logoColor=red)](https://youtu.be/7o0pfKDq9KE)

In first video of EP2P lecture about Kademlia Algorithm, we are going to gain brief understanding about elements of Kademlia nodes, network structure and Distributed Hash Tables (DHT) in theory.


[![](https://img.shields.io/badge/youtube-Kademlia%20Overview%202-FFF?style=for-the-badge&logo=youtube&logoColor=red)](https://www.youtube.com/watch?v=86tsT0g43iQ)

In this video we see some code example in Java for Kademlia API using an abstract library written by EP2P. **(The code used in this video is out dated)**
