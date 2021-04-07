/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.model;
import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.external.ExternalNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @brief Answer to a "FIND_NODE" query. Contains the nodes closest to an id given
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
@Getter
public class FindNodeAnswer<ID extends Number, C extends ConnectionInfo> extends Answer<ID> {
  private ID destinationId;
  /* Closest nodes in the answer. */
  private ArrayList<ExternalNode<ID, C>> nodes;

  public FindNodeAnswer() {
    nodes = new ArrayList<ExternalNode<ID, C>>();
    setAlive(true);
  }

  public FindNodeAnswer(ID destinationId) {
    this.destinationId = destinationId;
    nodes = new ArrayList<ExternalNode<ID, C>>();
    setAlive(true);
  }

  public int size() {
    return nodes.size();
  }

  public void remove(int index) {
    nodes.remove(index);
  }

  public void add(ExternalNode<ID, C> externalNode) {
    nodes.add(externalNode);
  }


  /**
   * @brief Merge the contents of this answer with another answer
   * @param findNodeAnswer another answer
   * @return number of nodes added to answer
   */
  public int merge(FindNodeAnswer<ID, C> findNodeAnswer) {
    int nbAdded = 0;

    for (ExternalNode<ID, C> c: findNodeAnswer.getNodes()) {
      if (!nodes.contains(c)) {
        nbAdded++;
        nodes.add(c);
      }
    }
    Collections.sort(nodes);
    //Trim the list
    while (findNodeAnswer.size() > Common.FIND_NODE_SIZE) {
      findNodeAnswer.remove(findNodeAnswer.size() - 1);
    }
    return nbAdded;
  }


  /**
   * @return if the destination has been found
   */
  public boolean destinationFound() {
    if (nodes.size() < 1) {
      return false;
    }
    ExternalNode<ID, C> tail = nodes.get(0);
    return ((Number) tail.getDistance()).equals(0);
  }

  @Override
  public String toString() {
    return "Answer [destinationId=" + destinationId + ", nodes=" + nodes + "]";
  }
}
