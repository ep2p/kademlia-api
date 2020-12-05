/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AbstractBucket<ID extends Number, C extends ConnectionInfo> implements Bucket<ID, C> {
  private static final long serialVersionUID = -6049494618368168254L;
  protected int id;
  protected List<ID> nodeIds;
  protected Map<ID, Node<ID, C>> nodeMap = new ConcurrentHashMap<>();

  /* Create a bucket for prefix `id` */
  public AbstractBucket(int id) {
    this.nodeIds = new CopyOnWriteArrayList<ID>();
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public int size() {
    return nodeIds.size();
  }

  public boolean contains(ID id) {
    return nodeIds.contains(id);
  }

  public boolean contains(Node<ID, C> node){
    return nodeIds.contains(node.getId());
  }

  /* Add a node to the front of the bucket */
  public void add(Node<ID, C> node) {
    nodeIds.add(0,node.getId());
    nodeMap.put(node.getId(), node);
  }

  public void remove(Node<ID, C> node){
    this.remove(node.getId());
  }

  public void remove(ID nodeId){
    nodeIds.remove(nodeId);
    nodeMap.remove(nodeId);
  }

  /* Push a node to the front of a bucket */
  /* Called when a node is already in bucket and brings them to front of the bucket as they are a living node */
  public synchronized void pushToFront(ID id) {
    nodeIds.remove(id);
    nodeIds.add(0, id);
  }

  public Node<ID, C> getNode(ID id) {
    return nodeMap.get(id);
  }

  public List<ID> getNodeIds() {
    return nodeIds;
  }

  @Override
  public String toString() {
    return "LongBucket [id= " + id + " nodeIds=" + nodeIds + "]";
  }
}
