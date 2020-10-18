/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.kademlia;


import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

public class RoutingTable implements Serializable {
  private static final long serialVersionUID = -6457490444295826198L;
  /* Bucket list */
  private Vector<Bucket> buckets;
  /* Id of the routing table owner */
  private int id;

  public RoutingTable(int id) {
    this.id = id;
    buckets = new Vector<Bucket>();
    for (int i = 0; i < Common.IDENTIFIER_SIZE + 1; i++) {
      buckets.add(new Bucket(i));
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
  public Bucket findBucket(int id) {
    int xorNumber = id ^ this.id;
    int prefix = this.getNodePrefix(xorNumber);
    return buckets.get(prefix);
  }

  /* Updates the routing table with a new value. */
  public void update(int id) {
    Bucket bucket = this.findBucket(id);
    if (bucket.contains(id)) {
      //If the element is already in the bucket, we update it.
      bucket.pushToFront(id);
    } else {
      bucket.add(id);
      if (bucket.size() > Common.BUCKET_SIZE)  {
        //TODO: Ping the least seen guy and remove him if he is offline.
      }
    }
  }

  /* Returns the closest nodes we know to a given id */
  public FindNodeAnswer findClosest(int destinationId) {
    FindNodeAnswer findNodeAnswer = new FindNodeAnswer(destinationId);
    Bucket bucket = this.findBucket(destinationId);
    BucketHelper.addToAnswer(bucket, findNodeAnswer, destinationId);

    // For every node (max common.BucketSize and lte identifier size) and add it to answer
    for (int i = 1; findNodeAnswer.size() < Common.BUCKET_SIZE && ((bucket.getId() - i) >= 0 ||
                                    (bucket.getId() + i) <= Common.IDENTIFIER_SIZE); i++) {
      //Check the previous buckets
      if (bucket.getId() - i >= 0) {
        Bucket bucketP = this.buckets.get(bucket.getId() - i);
        BucketHelper.addToAnswer(bucketP, findNodeAnswer, destinationId);
      }
      //Check the next buckets
      if (bucket.getId() + i <= Common.IDENTIFIER_SIZE) {
        Bucket bucketN = this.buckets.get(bucket.getId() + i);
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

  public Vector<Bucket> getBuckets() {
    return buckets;
  }

  @Override
  public String toString() {
    String string = "RoutingTable [ id=" + id + " " ;
    for (int i = 0; i < buckets.size(); i++) {
      if (buckets.get(i).size() > 0) {
        string += buckets.get(i) + " ";
      }
    }
    return string;
  }
}
