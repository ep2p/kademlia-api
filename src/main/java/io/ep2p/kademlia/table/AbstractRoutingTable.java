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
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.util.FindNodeAnswerReducer;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @param <I> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 * @param <B> Bucket type
 */
@NoArgsConstructor
public abstract class AbstractRoutingTable<I extends Number, C extends ConnectionInfo, B extends Bucket<I, C>> implements RoutingTable<I, C, B> {
  /* Bucket list */
  protected ArrayList<B> buckets;
  /* Id of the routing table owner (node id) */
  protected I id;
  protected transient NodeSettings nodeSettings;

  /**
   * @param id Node id of the table owner
   * @param nodeSettings Node setting
   */
  protected AbstractRoutingTable(I id, NodeSettings nodeSettings) {
    this.id = id;
    this.nodeSettings = nodeSettings;
    buckets = new ArrayList<>();
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
  public boolean update(Node<I, C> node) throws FullBucketException {
    //Setting last seen date on node

    ExternalNode<I, C> externalNode;

    if (!(node instanceof ExternalNode))
      externalNode = this.getExternalNode(node);
    else
      externalNode = (ExternalNode<I, C>) node;

    externalNode.setLastSeen(new Date());
    Bucket<I, C> bucket = this.findBucket(node.getId());
    if (bucket.contains(node)) {
      // If the element is already in the bucket, we update it and push it to the front of the bucket.
      bucket.pushToFront(externalNode);
      return false;
    } else if (bucket.size() < this.nodeSettings.getBucketSize()) {
      bucket.add(externalNode);
      return true;
    }
    throw new FullBucketException();
  }

  @Override
  public synchronized void forceUpdate(Node<I, C> node) {
    try {
      this.update(node);
    } catch (FullBucketException e) {
      Bucket<I, C> bucket = this.findBucket(node.getId());
      Date date = null;
      I oldestNode = null;
      for (I nodeId : bucket.getNodeIds()) {
        if (nodeId.equals(this.id)){
          continue;
        }
        if (date == null || bucket.getNode(nodeId).getLastSeen().before(date)){
          date = bucket.getNode(nodeId).getLastSeen();
          oldestNode = nodeId;
        }
      }
      bucket.remove(oldestNode);
      // recursive, because some other thread may add new node to the bucket while we are making a space
      this.forceUpdate(node);
    }
  }

  /**
   * Delete node from table
   * @param node to delete
   */
  public void delete(Node<I, C> node) {
    Bucket<I, C> bucket = this.findBucket(node.getId());
    bucket.remove(node);
  }


  /**
   * Returns the closest nodes we know to a given id
   * TODO: probably needs a better algorithm
   * @param destinationId lookup
   * @return result for closest nodes to destination
   */
  public FindNodeAnswer<I, C> findClosest(I destinationId) {
    FindNodeAnswer<I, C> findNodeAnswer = new FindNodeAnswer<>(destinationId);
    Bucket<I, C> bucket = this.findBucket(destinationId);
    BucketHelper.addToAnswer(bucket, findNodeAnswer, destinationId);

    // Loop over every bucket (max common.BucketSize or lte identifier size) and add it to answer
    for (int i = 1; findNodeAnswer.size() < this.nodeSettings.getBucketSize() && ((bucket.getId() - i) >= 0 ||
                                    (bucket.getId() + i) <= this.nodeSettings.getIdentifierSize()); i++) {
      //Check the previous buckets
      if (bucket.getId() - i >= 0) {
        Bucket<I, C> bucketP = this.buckets.get(bucket.getId() - i);
        BucketHelper.addToAnswer(bucketP, findNodeAnswer, destinationId);
      }
      //Check the next buckets
      if (bucket.getId() + i <= this.nodeSettings.getIdentifierSize()) {
        Bucket<I, C> bucketN = this.buckets.get(bucket.getId() + i);
        BucketHelper.addToAnswer(bucketN, findNodeAnswer, destinationId);
      }
    }

    //We sort the list
    Collections.sort(findNodeAnswer.getNodes());
    //We trim the list
    new FindNodeAnswerReducer<>(this.id, findNodeAnswer, this.nodeSettings.getFindNodeSize(), this.nodeSettings.getIdentifierSize()).reduce();
    while (findNodeAnswer.size() > this.nodeSettings.getFindNodeSize()) {
      findNodeAnswer.remove(findNodeAnswer.size() - 1); //TODO: Not the best thing.
    }
    return findNodeAnswer;
  }

  @Override
  public boolean contains(I nodeId) {
    Bucket<I, C> bucket = this.findBucket(nodeId);
    return bucket.contains(nodeId);
  }

  public List<B> getBuckets() {
    return buckets;
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder("LongRoutingTable [ id=" + id + " ");
    for (Bucket<I, C> bucket : buckets) {
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
