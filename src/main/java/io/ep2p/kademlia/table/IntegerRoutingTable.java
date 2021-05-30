/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;


import io.ep2p.kademlia.Common;
import io.ep2p.kademlia.connection.ConnectionInfo;

public class IntegerRoutingTable<C extends ConnectionInfo> extends AbstractRoutingTable<Integer, C, Bucket<Integer, C>> {

  private static final long serialVersionUID = 4367972615687933078L;

  public IntegerRoutingTable(Integer id) {
    super(id);
  }

  @Override
  protected IntegerBucket<C> createBucketOfId(int i) {
    return new IntegerBucket<>(i);
  }

  /**
   * @brief Returns an identifier which is in a specific bucket of a routing table
   * @param id id of the routing table owner
   * @param prefix id of the bucket where we want that identifier to be
   */
  public Integer getIdInPrefix(Integer id, int prefix) {
    if (prefix == 0) {
      return 0;
    }
    int identifier = 1;
    identifier = identifier << (prefix - 1);
    identifier = identifier ^ id;
    return identifier;
  }

  /* Returns the corresponding node prefix for a given id */
  public int getNodePrefix(Integer id) {
    for (int j = 0; j < Common.IDENTIFIER_SIZE; j++) {
      if ((id >> (Common.IDENTIFIER_SIZE - 1 - j) & 0x1) != 0) {
        return Common.IDENTIFIER_SIZE - j;
      }
    }
    return 0;
  }

  /* Finds the corresponding bucket in a routing table for a given identifier */
  public Bucket<Integer, C> findBucket(Integer id) {
    int xorNumber = (int) id ^ this.id;
    int prefix = this.getNodePrefix(xorNumber);
    return buckets.get(prefix);
  }
}
