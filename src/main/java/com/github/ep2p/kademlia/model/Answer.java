package com.github.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Model to hold base API call reply answer
 * @param <ID> Number type of node ID between supported types
 */
@Getter
@Setter
public class Answer<ID extends Number> {
    private ID nodeId;
    private boolean isAlive;
}
