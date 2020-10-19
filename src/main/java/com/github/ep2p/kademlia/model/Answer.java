package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Answer {
    private Integer nodeId;
    private boolean isAlive;
}
