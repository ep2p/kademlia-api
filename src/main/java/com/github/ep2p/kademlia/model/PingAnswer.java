package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PingAnswer {
    private int node;
    private boolean alive;
}
