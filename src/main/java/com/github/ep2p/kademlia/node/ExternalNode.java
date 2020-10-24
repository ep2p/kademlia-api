/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalNode<C extends ConnectionInfo> extends Node<C> implements Comparable<Object> {
  private int distance;

  public ExternalNode() {
  }

  public ExternalNode(Node<C> node, int distance) {
    setNode(node);
    this.distance = distance;
  }

  public int compareTo(Object o) {
    ExternalNode<C> c = (ExternalNode<C>) o;
    return Integer.compare(distance, c.distance);
  }

  @Override
  public String toString() {
    return "ExternalNode [id=" + getId() + ", distance=" + distance + "]";
  }

}