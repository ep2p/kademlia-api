/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.node;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ExternalNode<C extends ConnectionInfo> implements Comparable<Object> {
  private Node<C> node;
  private int distance;

  public ExternalNode(Node<C> node, int distance) {
    this.node = node;
    this.distance = distance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExternalNode<?> that = (ExternalNode<?>) o;
    return Objects.equals(getNode(), that.getNode());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getNode());
  }

  public int compareTo(Object o) {
    ExternalNode<C> c = (ExternalNode<C>) o;
    return Integer.compare(distance, c.distance);
  }

  @Override
  public String toString() {
    return "Contact [id=" + node.getId() + ", distance=" + distance + "]";
  }

}