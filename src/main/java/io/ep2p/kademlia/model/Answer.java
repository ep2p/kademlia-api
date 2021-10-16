package io.ep2p.kademlia.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Model to hold base API call reply answer
 * @param <ID> Number type of node ID between supported types
 */
@Getter
@Setter
public class Answer<ID extends Number> implements Serializable {
    private ID nodeId;
    private boolean isAlive;
}
