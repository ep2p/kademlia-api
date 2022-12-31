/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.node.external;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.decorators.DateAwareNodeDecorator;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Kademlia node from outside the system (other kademlia nodes are implementation of this class when they are seen in this system)
 * @param <I> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
@Getter
@Setter
public abstract class ExternalNode<I extends Number, C extends ConnectionInfo> extends DateAwareNodeDecorator<I, C> implements Comparable<Object> {
  protected I distance;

  protected ExternalNode(Node<I, C> node, I distance) {
    super(node);
    this.distance = distance;
  }

  public abstract int compareTo(@NotNull Object o);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExternalNode<?, ?> that = (ExternalNode<?, ?>) o;
    return Objects.equals(distance, that.distance) && Objects.equals(this.node, that.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(distance, getLastSeen());
  }

  @Override
  public String toString() {
    return "ExternalNode [id=" + getId() + ", distance=" + distance + "]";
  }
}