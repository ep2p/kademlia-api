---
title: "Kademlia API"
type: docs
---

# Kademlia API

Introduction
--------------

This API mainly focuses on an abstraction layer for Kademlia Algorithm, allows you to implement the network and storage layer in any ways you want.

However, DHT, node bootstrapping, and other communications logics are already implemented.  


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

## Installation

Using **maven central**

```xml
<dependency>
    <groupId>io.ep2p</groupId>
    <artifactId>kademlia-api</artifactId>
    <version>5.0.2-RELEASE</version>
</dependency>
```

GitHub releases page only contains certain releases. See [all maven releases](https://search.maven.org/artifact/io.ep2p/kademlia-api) in maven repository and choose the newest one there.

