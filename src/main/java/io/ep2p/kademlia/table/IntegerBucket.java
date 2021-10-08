/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;

public class IntegerBucket<C extends ConnectionInfo> extends AbstractBucket<Integer, C> {
  private static final long serialVersionUID = 4210483953921617691L;

  public IntegerBucket(int id) {
    super(id);
  }
}
