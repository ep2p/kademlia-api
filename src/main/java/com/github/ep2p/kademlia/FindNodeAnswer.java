/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.ExternalNode;

import java.util.ArrayList;
import java.util.Collections;

/* Answer to a "FIND_NODE" query. Contains the nodes closest to an id given */
public class FindNodeAnswer<C extends ConnectionInfo> {
  private int destinationId;
  /* Closest nodes in the answer. */
  private ArrayList<ExternalNode<C>> nodes;

  public FindNodeAnswer(int destinationId) {
    this.destinationId = destinationId;
    nodes = new ArrayList<ExternalNode<C>>();
  }

  int getDestinationId() {
    return destinationId;
  }

  public ArrayList<ExternalNode<C>> getNodes() {
    return nodes;
  }

  public int size() {
    return nodes.size();
  }

  public void remove(int index) {
    nodes.remove(index);
  }

  public void add(ExternalNode<C> externalNode) {
    nodes.add(externalNode);
  }

  /* Merge the contents of this answer with another answer */
  public int merge(FindNodeAnswer<C> findNodeAnswer) {
    int nbAdded = 0;

    for (ExternalNode<C> c: findNodeAnswer.getNodes()) {
      if (!nodes.contains(c)) {
        nbAdded++;
        nodes.add(c);
      }
    }
    Collections.sort(nodes);
    //Trim the list
    while (findNodeAnswer.size() > Common.BUCKET_SIZE) {
      findNodeAnswer.remove(findNodeAnswer.size() - 1);
    }
    return nbAdded;
  }

  /* Returns if the destination has been found */
  public boolean destinationFound() {
    if (nodes.size() < 1) {
      return false;
    }
    ExternalNode<C> tail = nodes.get(0);
    return tail.getDistance() == 0;
  }

  @Override
  public String toString() {
    return "Answer [destinationId=" + destinationId + ", nodes=" + nodes + "]";
  }
}
