/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;


import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.node.Node;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Date;
import java.util.Vector;

/**
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 * @param <B> Bucket type
 */
@NoArgsConstructor
public abstract class AbstractRoutingTable<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> implements RoutingTable<ID, C, B> {
  /* Bucket list */
  protected Vector<B> buckets;
  /* Id of the routing table owner (node id) */
  protected ID id;
  protected NodeSettings nodeSettings;

  /**
   * @param id Node id of the table owner
   */
  public AbstractRoutingTable(ID id, NodeSettings nodeSettings) {
    this.id = id;
    this.nodeSettings = nodeSettings;
    buckets = new Vector<B>();
    for (int i = 0; i < nodeSettings.getIdentifierSize() + 1; i++) {
      buckets.add(createBucketOfId(i));
    }
  }

  protected abstract B createBucketOfId(int i);


  /**
   * Updates the routing table with a new value. Returns true if node didnt exist in table before
   * @param node to add or update (push to front)
   * @return if node is added newly (not updated)
   */
  public boolean update(Node<ID, C> node) throws FullBucketException {
    //Setting last seen date on node
    node.setLastSeen(new Date());
    Bucket<ID, C> bucket = this.findBucket(node.getId());
    if (bucket.contains(node)) {
      //If the element is already in the bucket, we update it and push it to the front of the bucket.
      bucket.pushToFront(node.getId());
      return false;
    } else if (bucket.size() < this.nodeSettings.getBucketSize()) {
      bucket.add(node);
      return true;
    }
    throw new FullBucketException();
  }


  /**
   * Delete node from table
   * @param node to delete
   */
  public void delete(Node<ID, C> node) {
    Bucket<ID, C> bucket = this.findBucket(node.getId());
    bucket.remove(node);
  }


  /**
   * Returns the closest nodes we know to a given id
   * @param destinationId lookup
   * @return result for closest nodes to destination
   */
  public FindNodeAnswer<ID, C> findClosest(ID destinationId) {
    FindNodeAnswer<ID, C> findNodeAnswer = new FindNodeAnswer<ID, C>(destinationId);
    Bucket<ID, C> bucket = this.findBucket(destinationId);
    BucketHelper.addToAnswer(bucket, findNodeAnswer, destinationId);

    // For every node (max common.BucketSize and lte identifier size) and add it to answer
    for (int i = 1; findNodeAnswer.size() < this.nodeSettings.getBucketSize() && ((bucket.getId() - i) >= 0 ||
                                    (bucket.getId() + i) <= this.nodeSettings.getIdentifierSize()); i++) {
      //Check the previous buckets
      if (bucket.getId() - i >= 0) {
        Bucket<ID, C> bucketP = this.buckets.get(bucket.getId() - i);
        BucketHelper.addToAnswer(bucketP, findNodeAnswer, destinationId);
      }
      //Check the next buckets
      if (bucket.getId() + i <= this.nodeSettings.getIdentifierSize()) {
        Bucket<ID, C> bucketN = this.buckets.get(bucket.getId() + i);
        BucketHelper.addToAnswer(bucketN, findNodeAnswer, destinationId);
      }
    }

    //We sort the list
    Collections.sort(findNodeAnswer.getNodes());
    //We trim the list
    while (findNodeAnswer.size() > this.nodeSettings.getIdentifierSize()) {
      findNodeAnswer.remove(findNodeAnswer.size() - 1); //TODO: Not the best thing.
    }
    return findNodeAnswer;
  }

  public Vector<B> getBuckets() {
    return buckets;
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder("LongRoutingTable [ id=" + id + " ");
    for (Bucket<ID, C> bucket : buckets) {
      if (bucket.size() > 0) {
        string.append(bucket.getId()).append(" ");
      }
    }
    return string.toString();
  }

  public NodeSettings getNodeSettings() {
    return nodeSettings;
  }
}
