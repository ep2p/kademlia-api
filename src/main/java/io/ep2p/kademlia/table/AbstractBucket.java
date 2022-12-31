/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @param <I> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
public class AbstractBucket<I extends Number, C extends ConnectionInfo> implements Bucket<I, C> {
  private static final long serialVersionUID = -6049494618368168254L;
  protected final int id;
  protected final CopyOnWriteArrayList<I> nodeIds;
  protected final ConcurrentHashMap<I, ExternalNode<I, C>> nodeMap = new ConcurrentHashMap<>();

  /**
   * Create a bucket for prefix `id`
   * @param id prefix
   */
  public AbstractBucket(int id) {
    this.nodeIds = new CopyOnWriteArrayList<>();
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public int size() {
    return nodeIds.size();
  }

  public boolean contains(I id) {
    return nodeIds.contains(id);
  }

  public boolean contains(Node<I, C> node){
    return nodeIds.contains(node.getId());
  }

  /**
   * @param node to add to this bucket
   */
  @Override
  public void add(ExternalNode<I, C> node) {
    nodeIds.add(0,node.getId());
    nodeMap.put(node.getId(), node);
  }

  @Override
  public void remove(Node<I, C> node){
    this.remove(node.getId());
  }

  @Override
  public void remove(I nodeId){
    nodeIds.remove(nodeId);
    nodeMap.remove(nodeId);
  }

  /**
   * @param node the node to push
   */
  @Override
  public synchronized void pushToFront(ExternalNode<I, C> node) {
    nodeIds.remove(node.getId());
    nodeIds.add(0, node.getId());
    nodeMap.get(node.getId()).setLastSeen(node.getLastSeen());
  }

  @Override
  public ExternalNode<I, C> getNode(I id) {
    return nodeMap.get(id);
  }

  @Override
  public List<I> getNodeIds() {
    return nodeIds;
  }

  @Override
  public String toString() {
    return "LongBucket [id= " + id + " nodeIds=" + nodeIds + "]";
  }
}
