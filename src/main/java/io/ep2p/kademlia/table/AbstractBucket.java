/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
public class AbstractBucket<ID extends Number, C extends ConnectionInfo> implements Bucket<ID, C> {
  private static final long serialVersionUID = -6049494618368168254L;
  protected final int id;
  protected final List<ID> nodeIds;
  protected final Map<ID, ExternalNode<ID, C>> nodeMap = new ConcurrentHashMap<>();

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

  public boolean contains(ID id) {
    return nodeIds.contains(id);
  }

  public boolean contains(Node<ID, C> node){
    return nodeIds.contains(node.getId());
  }

  /**
   * @param node to add to this bucket
   */
  @Override
  public void add(ExternalNode<ID, C> node) {
    nodeIds.add(0,node.getId());
    nodeMap.put(node.getId(), node);
  }

  @Override
  public void remove(Node<ID, C> node){
    this.remove(node.getId());
  }

  @Override
  public void remove(ID nodeId){
    nodeIds.remove(nodeId);
    nodeMap.remove(nodeId);
  }

  /**
   * @param id of the node to push
   */
  @Override
  public synchronized void pushToFront(ID id) {
    nodeIds.remove(id);
    nodeIds.add(0, id);
  }

  @Override
  public ExternalNode<ID, C> getNode(ID id) {
    return nodeMap.get(id);
  }

  @Override
  public List<ID> getNodeIds() {
    return nodeIds;
  }

  @Override
  public String toString() {
    return "LongBucket [id= " + id + " nodeIds=" + nodeIds + "]";
  }
}
