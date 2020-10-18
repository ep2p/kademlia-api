/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia;
import java.io.Serializable;
import java.util.ArrayList;

//TODO: Bucket should not only keep id of nodes, but also connection details
public class Bucket implements Serializable {
  private static final long serialVersionUID = -1722492430545836476L;

  private ArrayList<Integer> nodes;
  private int id;

  /* Create a bucket for prefix `id` */
  public Bucket(int id) {
    this.nodes = new ArrayList<Integer>();
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public int size() {
    return nodes.size();
  }

  public boolean contains(int id) {
    return nodes.contains(id);
  }

  /* Add a node to the front of the bucket */
  public void add(int id) {
    nodes.add(0,id);
  }

  /* Push a node to the front of a bucket */
  /* Called when a node is already in bucket and brings them to front of the bucket as they are a living node */
  public void pushToFront(int id) {
    int i = nodes.indexOf(id);
    nodes.remove(i);
    nodes.add(0, id);
  }

  public int getNode(int id) {
    return nodes.get(id);
  }

  public ArrayList<Integer> getNodes() {
    return nodes;
  }

  @Override
  public String toString() {
    return "Bucket [id= " + id + " nodes=" + nodes + "]";
  }
}
