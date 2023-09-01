---
title: "Examples"
weight: 4
---

# Examples

Assume we have a network of guid size 4, meaning there can be maximum 2^4 (16) nodes available in the network.

As discussed in [Kademlia Node Usage]({{< relref "/docs/usage/kademlia-usage.md" >}}), in real world examples, your node ids are unique and are generated through an algorithm that can proof a node id uniqueness to other nodes.
We don't do that in this example, we just assume a node as some integer id.

## Node instantiation

To begin with, we have to set some settings on how we want our network to behave. These settings are mutual between all nodes in the network.


```java
// Determining Identifier Size to 4, meaning we can have 2^4 nodes in network
NodeSettings.Default.IDENTIFIER_SIZE = 4;
// Determining the size of each routing table bucket size (we are limited to fixed bucket sizes in this implementation of kademlia) 
NodeSettings.Default.BUCKET_SIZE = 16;

// Determining how often we want to ping other nodes to assure they are alive
// This helps to give certain nodes better chance to stay alive in our routing table
// Setting the ping scheduler time to 5 seconds
NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
NodeSettings.Default.PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;

// Other node settings are explained in Kademlia Node Usage documentation
```

We assume you have implemented the `ConnectionInfo` class as explained in Kademlia Node Usage documentation.
For our example purpose, the implementation is called: `EmptyConnectionInfo`

Next step is to create a routing table. Again, default implementations support Integer, Long and BigInteger, and the value is stored on memory and is lost over program restarts.

```java
// First we build a NodeSettings instance from the Default values we filled earlier 
NodeSettings nodeSettings = NodeSettings.Default.build();

// Since the routing table factory requires NodeSetting information we pass it to the factory
RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(nodeSettings);

// Our routing table factory is ready. We can get routing table instances from this factory further on.
```

In this example we want to create a "DHT Kademlia Node". The hash table is `Key -> Value` storage. A key can be in any type of course.

Next things we have to determine are:

- What is the Java class type of our DHT keys and values
- How do we hash and boung a key into the size of our network (imagine a key hash/value is 100 while we are in network size of 4 (16 nodes), we need to determine which node would be known as closest node to this key)

The `KeyHashGenerator` interface is supposed to take a key as input and return a value which is bounded to the size of the network.

```java
// Assuming our keys are Integer and the network node IDs is Integer too. We already implemented SampleKeyHashGenerator to cover some of these cases.
KeyHashGenerator<Integer, Integer> keyHashGenerator = new SampleKeyHashGenerator(NodeSettings.Default.IDENTIFIER_SIZE);
```

Since we are implementing DHT, we need a "Repository/Storage" to keep the data in. You should implement your own.

```java
KademliaRepository<Integer, String> repository = new SampleRepository<>();
```

Next step we need to implement communication protocol for nodes, AKA "Message Sender". This was explained in the [Abstraction]({{< relref "/docs/usage/abstraction.md" >}}) section.

```java
MessageSender<Integer, EmptyConnectionInfo> messageSenderAPI = ...; // Your implementation.
```


You are almost done, lets instantiate a KademliaDHTNode from everything we prepared so far:

```java
// This node can story Integer -> String maps in its storage, and the Node IDs are in Integer format. The ID of the node is 0.
int nodeId = 0;
DHTKademliaNodeAPI<Integer, EmptyConnectionInfo, Integer, String> node = new DHTKademliaNodeBuilder<>(
        nodeId,
        new EmptyConnectionInfo(),
        routingTableFactory.getRoutingTable(nodeId),
        messageSenderAPI,
        keyHashGenerator,
        repository
).build();

// Start the node
node.start()
```

## API

If you already have nodes in the network you can bootstrap them using other nodes:

```java
Node<Integer, EmptyConnectionInfo> otherNode = ...;  // presentation of another node in the system
Future<Boolean> bootstrapped = myNode.start(otherNode);
System.out.print("Could I bootstrap? " + bootstrapped.get())
```

To store data into DHT:

```java

String data = "something";
Integer key = data.hashCode();

Future<StoreAnswer<Integer, EmptyConnectionInfo, Integer>> storeFuture = node.store(key, data);
StoreAnswer<Integer, EmptyConnectionInfo, Integer> storeAnswer = storeFuture.get();
System.out.println(storeAnswer.getNode().getId() + " stored " + storeAnswer.getKey());
```

To look up for data in DHT:

```java
Future<LookupAnswer<Integer, EmptyConnectionInfo, Integer, String>> lookupFuture = node.lookup(key);
LookupAnswer<Integer, EmptyConnectionInfo, Integer, String> lookupAnswer = lookupFuture.get();

Assertions.assertEquals(LookupAnswer.Result.FOUND, lookupAnswer.getResult());
System.out.println(lookupAnswer.getNode().getId() + " returned the data");
```

An example of a bootstrapped network with 16 nodes is available in this [test case](https://github.com/ep2p/kademlia-api/blob/main/src/test/java/io/ep2p/kademlia/DHTTest.java) inside this repository.

For Netty implementation, you can find something similar (2 nodes only) in [this test case](https://github.com/ep2p/kademlia-netty/blob/main/src/test/java/io/ep2p/kademlia/netty/DHTTest.java).


## More real example

You can check [Kademlia Netty Standalone Test](https://github.com/ep2p/kademlia-netty-standalone-test) repository which is a runnable project that helps you get a sense of all of this.
It's implemented using Spring Shell to let you interact with it.

