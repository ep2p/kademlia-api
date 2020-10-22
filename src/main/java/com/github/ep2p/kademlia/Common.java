/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package com.github.ep2p.kademlia;

public class Common {
  public static long BOOTSTRAP_NODE_CALL_TIMEOUT_SEC = 100;
  public static long STORE_TIMEOUT_SEC = 20;
  public static int ALPHA = 3;
  public static int IDENTIFIER_SIZE = 6;
  public static int REFERENCED_NODES_UPDATE_PERIOD_SEC = 30;
  /* Maximum size of the buckets */
  public static int BUCKET_SIZE = 20;
  public static int JOIN_BUCKETS_QUERIES = 1;
  public static int LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE = 20;
}
