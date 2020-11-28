/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.node.external;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ExternalNode<ID extends Number, C extends ConnectionInfo> extends Node<ID, C> implements Comparable<Object> {
  protected ID distance;

  public ExternalNode() {
  }

  public ExternalNode(Node<ID, C> node, ID distance) {
    setNode(node);
    this.distance = distance;
  }

  public abstract int compareTo(Object o);

  @Override
  public String toString() {
    return "ExternalNode [id=" + getId() + ", distance=" + distance + "]";
  }

}