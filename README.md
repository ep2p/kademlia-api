# eleuth-java-kademlia-api
Java Kademlia API for Eleuth network.

This API mainly focuses on routing table and peer choosing logic, and moves data persistence & networking to abstraction layer.
Therefor, you can choose how you want to communicate between peers (You can use TCP socket, or even HTTP or Websocket) by implementing interfaces.
