---
title: "Abstraction"
weight: 2
---

# Abstraction

In this section, we go through the abstraction layer and what needs to be implemented in order for this API to work.

## Node ID

You should make a decision about size of your node IDs. This API supports `Integer`, `Long` and `BigInteger` by default.
How a node ID is chosen can completely be different per use case.
For example, [this article](https://medium.com/coinmonks/a-brief-overview-of-kademlia-and-its-use-in-various-decentralized-platforms-da08a7f72b8f) suggests why it's a good idea to have the hash of your node's public key as your Node ID.

## Connection Info

This is just a `Serializable` interface that determines how nodes can communicate with each other. For example, one implementation can be a simple `ip` and `port` of a TCP socket.

## Node

Each kademlia node only sees other nodes as combination of **Node ID** and **Connection Info**.

## RoutingTable
`RoutingTable` and its buckets are Serializable. So you will easily be able to write it to a file.
When you are creating an instance of your node, you can pass a new routing table or use one from disk.
At this stage, there is no helper class for writing routing table on disk.

You can get proper implementation of routing table based on the ID type of your nodes. Available ones are: `BigIntegerRoutingTable`, `LongRoutingTable` and `IntegerRoutingTable`.

## Protocol

### Kademlia Message

These are objects that represent a message that should be sent and delivered to other nodes.
The default package where messages exist is `io.ep2p.kademlia.protocol.message`.
Each message has certain type of serializable data attached to it, and it includes repliers' `Node` and status of the replier (if it's alive or not).

Each message has a type as well, which helps you during the serialization/deserialization process to determine which object should be created from the message.


| Type              | KademliaMessage Class          | Purpose                                                |
|-------------------|--------------------------------|--------------------------------------------------------|
| EMPTY             | EmptyKademliaMessage           | Just proves another message was delivered/accepted     |
| PING              | PingKademliaMessage            | Ping data to be sent to other nodes                    |
| PONG              | PongKademliaMessage            | Ping response                                          |
| FIND_NODE_REQ     | FindNodeRequestMessage         | Request to find nodes close to current node            |
| FIND_NODE_RES     | FindNodeResponseMessage        | Response to find node request                          |
| SHUTDOWN          | ShutdownKademliaMessage        | To tell other nodes that current node is shutting down |
| DHT_LOOKUP        | DHTLookupKademliaMessage       | Lookup for data in DHT                                 |
| DHT_LOOKUP_RESULT | DHTLookupResultKademliaMessage | Result of lookup in DHT                                |
| DHT_STORE         | DHTStoreKademliaMessage        | Store data in DHT                                      |
| DHT_STORE_RESULT  | DHTStoreResultKademliaMessage  | Result of storing data in DHT                          |


### Message Sender

This is the networking layer. Should be implemented to send message (`KademliaMessage`) from the `caller` kademlia-node to `receiver` node.
Serialization, Validation, and request sending are the basic things that shall be done here.
The output of the API call probably has a response which should still be converted to the proper `KademliaMessage` and returned to the caller.

### Custom Message handler

In case you want to extend the protocol or change behaviours, you should add new or extend messages, and write custom [handlers](https://github.com/ep2p/kademlia-api/blob/main/src/main/java/io/ep2p/kademlia/protocol/handler/MessageHandler.java).
You can then register your message handler on your `KademliaNodes` using [`kademliaNode.registerMessageHandler(type, handler)`](https://github.com/ep2p/kademlia-api/blob/3d37f08cee9687471e18779b051aef19389215b4/src/main/java/io/ep2p/kademlia/node/KademliaNodeAPI.java#L68)

## Key Hash Generator

The purpose of [`KeyHashGenerator`](https://github.com/ep2p/kademlia-api/blob/3d37f08cee9687471e18779b051aef19389215b4/src/main/java/io/ep2p/kademlia/node/KeyHashGenerator.java) interface is to bound DHT key size to network GUID size.

For example: when node IDs are `BigInteger` but DHT keys are `String`, a `KeyHashGenerator` implementation is needed to return hash of the `String` key as `BigInteger` value. 

## Kademlia Repository

This is simply DHT repository where keys and values are stored.
