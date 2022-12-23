---
title: "Kademlia"
weight: 3
---

# Kademlia Usage

In this section, we see how to create your Kademlia Nodes. Make sure you have read [abstraction layer]({{< relref "/docs/usage/abstraction.md" >}}) first.

## Introduction

This library has 2 Kademlia related _interfaces_.

- `KademliaNodeAPI`: Main implementation is `KademliaNode` class. It only handles bootstrapping the network and keeping it alive.
- `KademliaDHTNodeAPI`: _Extends_ `KademliaNodeAPI` interface. Main implementation is `KademliaDHTNode` which [_decorates_](https://en.wikipedia.org/wiki/Decorator_pattern) any `KademliaNodeAPI`.

Therefore, to create `KademliaDHTNode` you first need an instance of `KademliaNodeAPI` (`KademliaNode`).

It's not as complicated as it may sound. Read below.

## Kademlia Node

For only network bootstrapping and continues routing-table updates you can consider using default `KademliaNodeAPI (interface)` implementation: `KademliaNode (impelementation)`.

```java
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.KademliaNode;

KademliaNodeAPI<ID, C> node = new KademliaNode<>(id, connectionionInfo, routingTable, messageSenderAPI, nodeSettings);
```

You can start the node by calling `start()` method, or you can bootstrap it by calling `bootstrap(bootstrapNode)` method and pass `Node<ID, C>` of bootstrap node as the parameter.

You can stop the node by calling `stop()` method which should gracefully stop the node and its executor services, or you can stop it by calling `stopNow()` method which will immediately the running jobs.

### Generics
Note that KademliaNode and all its subclasses are Generic.

The first generic type is for ID of the node, and you can choose between `Integer`, `Long`, `BigInteger` as these are the only ones supported and you should decide on which on to use based on your GUID space size. For example on **Eleuth Node System** Biginteger is being used since node IDs are `SHA1` of a public keys. You can [choose the appropriate `RoutingTable`](https://github.com/ep2p/kademlia-api/tree/main/src/main/java/io/ep2p/kademlia/table) implementation based on this key size. There is an implementation available for all of the three supported ID types.

The second generic type is type of your class that implements `ConnectionInfo`.

## DHT Kademlia Node

For **Distributed Hash Table** you should consider using `DHTKademliaNodeAPI` interface. The main implementation is `DHTKademliaNode` class.

This class decorates `KademliaNodeAPI` as explained earlier. So you can instantiate it in 2 ways.

A. Using constructor:

```java
new DHTKademliaNode<>(kademliaNodeAPI, keyHashGenerator, kademliaRepository);
```

B. Using builder:

```java
new DHTKademliaNodeBuilder<>(id, connectionInfo, routingTable, messageSenderAPI, keyHashGenerator, kademliaRepository).build();
```


## Settings

You can configure your node by passing an instance of `NodeSettings` to `KademliaNode` or it's subclasses.
By default, they use the configuration from `NodeSettings.Default` which has static fields. You can edit `NodeSettings.Default` if all the nodes that you want to have in a single application are having the same configuration.

Node Settings explanation:

| Name                                    | Info                                                                                                                                                                                                             |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| IDENTIFIER_SIZE                         | GUID Space AKA Routing table buckets count                                                                                                                                                                       |
| BUCKET_SIZE                             | Max size of each bucket in routing table                                                                                                                                                                         |
| FIND_NODE_SIZE                          | Size of results list when finding close nodes                                                                                                                                                                    |
| MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE | Value in seconds. If a node is older than this age, we will ping it before sending messages                                                                                                                      |
| PING_SCHEDULE_TIME_VALUE                | Value in `PING_SCHEDULE_TIME_UNIT` to send pings in scheduler                                                                                                                                                    |
| PING_SCHEDULE_TIME_UNIT                 | TimeUnit for ping scheduler                                                                                                                                                                                      |
| DHT_EXECUTOR_POOL_SIZE                  | Size of ExecutorService in DHT for default constructor                                                                                                                                                           |
| ENABLED_FIRST_STORE_REQUEST_FORCE_PASS  | Enables force pass on first store loop. Has negative impact on performance but makes sure another node also tries to store. Useful in situations where a node doesn't know much about other nodes in the network |


## Republishing Keys

In DHT we often require to republish keys. For example in a scenario where a node is shutting down there should be a mechanism for the node to republish its keys (and values) to other nodes before shutting down.
This way there won't be much data loss. Another scenario is when a new node joins the network, and we (as an individual node) may want to pass the data that the new node should hold to them.
Therefore, we should periodically republish our data so closer nodes hold it.

This kademlia-api does not implement key republishing since `4.0.0-RELEASE` version. This mechanism can be very tricky and very depended on how each developer implements their `KademliaRepository`.
For example, when a single key can hold large amount of data we'd probably want to find a closer node first and then pass the data to them in different chunks.

