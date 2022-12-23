[![Documentation](https://img.shields.io/badge/documentation-white?style=flat&logo=hugo&logoColor=7289DA)](https://ep2p.github.io/kademlia-api/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.ep2p/kademlia-api/badge.png?gav=true)](https://maven-badges.herokuapp.com/maven-central/io.ep2p/kademlia-api)
[![Github Releases](https://badgen.net/github/release/ep2p/kademlia-api)](https://github.com/ep2p/kademlia-api/releases)
[![Open Issues](https://badgen.net/github/open-issues/ep2p/kademlia-api)](https://github.com/ep2p/kademlia-api/issues)
[![Liscence](https://badgen.net/github/license/ep2p/kademlia-api)](https://github.com/ep2p/kademlia-api/blob/main/LICENSE)

# Kademlia Api
Abstract Java Kademlia API

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

---

## Documentation

You can access the [full documentation](https://ep2p.github.io/kademlia-api/) in our Hugo hosted website :)

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
