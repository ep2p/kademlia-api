/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;


import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.node.external.LongExternalNode;

public class LongRoutingTable<C extends ConnectionInfo> extends AbstractRoutingTable<Long, C, LongBucket<C>> {

  private static final long serialVersionUID = 3002310308952493026L;

  public LongRoutingTable(Long id, NodeSettings nodeSettings) {
    super(id, nodeSettings);
  }

  @Override
  protected LongBucket<C> createBucketOfId(int i) {
    return new LongBucket<>(i);
  }

  @Override
  public ExternalNode<Long, C> getExternalNode(Node<Long, C> node) {
    return new LongExternalNode<>(node, this.getDistance(node.getId()));
  }

  /**
   * Returns an identifier which is in a specific bucket of a routing table
   * @param id id of the routing table owner
   * @param prefix id of the bucket where we want that identifier to be
   */
  public Long getIdInPrefix(Long id, int prefix) {
    if (prefix == 0) {
      return 0L;
    }
    long identifier = 1;
    identifier = identifier << (prefix - 1);
    identifier = identifier ^ id;
    return identifier;
  }

  /* Returns the corresponding node prefix for a given id */
  public int getNodePrefix(Long id) {
    for (int j = 0; j < this.nodeSettings.getIdentifierSize(); j++) {
      if ((id >> (this.nodeSettings.getIdentifierSize() - 1 - j) & 0x1) != 0) {
        return this.nodeSettings.getIdentifierSize() - j;
      }
    }
    return 0;
  }

  /* Finds the corresponding bucket in a routing table for a given identifier */
  public LongBucket<C> findBucket(Long id) {
    long xorNumber = this.getDistance(id);
    int prefix = this.getNodePrefix(xorNumber);
    return buckets.get(prefix);
  }

  @Override
  public Long getDistance(Long id) {
    return id ^ this.id;
  }
}
