/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia.table;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.node.Node;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Bucket<C extends ConnectionInfo> implements Serializable {
  private static final long serialVersionUID = -8295900946418424049L;
  private int id;
  private List<Integer> nodeIds;
  private Map<Integer, Node<C>> nodeMap = new ConcurrentHashMap<>();

  /* Create a bucket for prefix `id` */
  public Bucket(int id) {
    this.nodeIds = new CopyOnWriteArrayList<Integer>();
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public int size() {
    return nodeIds.size();
  }

  public boolean contains(int id) {
    return nodeIds.contains(id);
  }

  public boolean contains(Node<C> node){
    return nodeIds.contains(node.getId());
  }

  /* Add a node to the front of the bucket */
  public void add(Node<C> node) {
    nodeIds.add(0,node.getId());
    nodeMap.put(node.getId(), node);
  }

  public void remove(Node<C> node){
    this.remove(node.getId());
  }

  public void remove(int nodeId){
    nodeIds.remove((Integer) nodeId);
    nodeMap.remove(nodeId);
  }

  /* Push a node to the front of a bucket */
  /* Called when a node is already in bucket and brings them to front of the bucket as they are a living node */
  public synchronized void pushToFront(int id) {
    nodeIds.remove((Integer) id);
    nodeIds.add(0, id);
  }

  public Node<C> getNode(int id) {
    return nodeMap.get(id);
  }

  public List<Integer> getNodeIds() {
    return nodeIds;
  }

  @Override
  public String toString() {
    return "Bucket [id= " + id + " nodeIds=" + nodeIds + "]";
  }
}
