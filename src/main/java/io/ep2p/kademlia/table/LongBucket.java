/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.ep2p.kademlia.table;

import io.ep2p.kademlia.connection.ConnectionInfo;

public class LongBucket<C extends ConnectionInfo> extends AbstractBucket<Long, C> {
  private static final long serialVersionUID = -6314496739729756505L;

  public LongBucket(int id) {
    super(id);
  }
}
