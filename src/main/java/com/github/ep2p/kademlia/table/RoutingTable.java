/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.table;


import com.github.ep2p.kademlia.Common;
import com.github.ep2p.kademlia.FindNodeAnswer;
import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

public class RoutingTable<C extends ConnectionInfo> implements Serializable {
  private static final long serialVersionUID = 7920534415564909219L;

  /* Bucket list */
  private Vector<Bucket<C>> buckets;
  /* Id of the routing table owner */
  private int id;

  public RoutingTable(int id) {
    this.id = id;
    buckets = new Vector<Bucket<C>>();
    for (int i = 0; i < Common.IDENTIFIER_SIZE + 1; i++) {
      buckets.add(new Bucket<C>(i));
    }
  }

  /**
   * @brief Returns an identifier which is in a specific bucket of a routing table
   * @param id id of the routing table owner
   * @param prefix id of the bucket where we want that identifier to be
   */
  public int getIdInPrefix(int id, int prefix) {
    if (prefix == 0) {
      return 0;
    }
    int identifier = 1;
    identifier = identifier << (prefix - 1);
    identifier = identifier ^ id;
    return identifier;
  }

  /* Returns the corresponding node prefix for a given id */
  public int getNodePrefix(int id) {
    for (int j = 0; j < Common.IDENTIFIER_SIZE; j++) {
      if ((id >> (Common.IDENTIFIER_SIZE - 1 - j) & 0x1) != 0) {
        return Common.IDENTIFIER_SIZE - j;
      }
    }
    return 0;
  }

  /* Finds the corresponding bucket in a routing table for a given identifier */
  public Bucket<C> findBucket(int id) {
    int xorNumber = id ^ this.id;
    int prefix = this.getNodePrefix(xorNumber);
    return buckets.get(prefix);
  }

  /* Updates the routing table with a new value. */
  public void update(Node<C> node) {
    Bucket<C> bucket = this.findBucket(node.getId());
    if (bucket.contains(node)) {
      //If the element is already in the bucket, we update it.
      bucket.pushToFront(node.getId());
    } else {
      bucket.add(node);
      if (bucket.size() > Common.BUCKET_SIZE)  {
        //TODO: Ping the least seen guy and remove him if he is offline.
      }
    }
  }

  /* Returns the closest nodes we know to a given id */
  public FindNodeAnswer<C> findClosest(int destinationId) {
    FindNodeAnswer<C> findNodeAnswer = new FindNodeAnswer<C>(destinationId);
    Bucket<C> bucket = this.findBucket(destinationId);
    BucketHelper.addToAnswer(bucket, findNodeAnswer, destinationId);

    // For every node (max common.BucketSize and lte identifier size) and add it to answer
    for (int i = 1; findNodeAnswer.size() < Common.BUCKET_SIZE && ((bucket.getId() - i) >= 0 ||
                                    (bucket.getId() + i) <= Common.IDENTIFIER_SIZE); i++) {
      //Check the previous buckets
      if (bucket.getId() - i >= 0) {
        Bucket<C> bucketP = this.buckets.get(bucket.getId() - i);
        BucketHelper.addToAnswer(bucketP, findNodeAnswer, destinationId);
      }
      //Check the next buckets
      if (bucket.getId() + i <= Common.IDENTIFIER_SIZE) {
        Bucket<C> bucketN = this.buckets.get(bucket.getId() + i);
        BucketHelper.addToAnswer(bucketN, findNodeAnswer, destinationId);
      }
    }

    //We sort the list
    Collections.sort(findNodeAnswer.getNodes());
    //We trim the list
    while (findNodeAnswer.size() > Common.BUCKET_SIZE) {
      findNodeAnswer.remove(findNodeAnswer.size() - 1); //TODO: Not the best thing.
    }
    return findNodeAnswer;
  }

  public Vector<Bucket<C>> getBuckets() {
    return buckets;
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder("RoutingTable [ id=" + id + " ");
    for (Bucket<C> bucket : buckets) {
      if (bucket.size() > 0) {
        string.append(bucket.getId()).append(" ");
      }
    }
    return string.toString();
  }
}
