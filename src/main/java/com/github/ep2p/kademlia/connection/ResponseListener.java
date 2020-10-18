package com.github.ep2p.kademlia.connection;

public interface ResponseListener<R> {
    void onResponse(R response);
    void onError();
}
