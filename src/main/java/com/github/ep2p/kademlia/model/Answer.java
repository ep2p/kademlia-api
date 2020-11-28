package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Answer<ID extends Number> {
    private ID nodeId;
    private boolean isAlive;
}
