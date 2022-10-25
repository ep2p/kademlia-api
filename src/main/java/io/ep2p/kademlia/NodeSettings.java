package io.ep2p.kademlia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeSettings implements Serializable {
  public int alpha;
  public int identifierSize;
  /* Maximum size of the buckets */
  public int bucketSize;
  public int findNodeSize;
  public int maximumLastSeenAgeToConsiderAlive;

  public int pingScheduleTimeValue;
  public TimeUnit pingScheduleTimeUnit;
  public int dhtExecutorPoolSize;
  public int dhtScheduledExecutorPoolSize;
  public int maximumStoreAndLookupTimeoutValue;
  public TimeUnit maximumStoreAndGetTimeoutTimeUnit;
  public boolean enabledFirstStoreRequestForcePass;


  public static class Default {
    public static int ALPHA = 10;
    public static int IDENTIFIER_SIZE = 128;
    public static int BUCKET_SIZE = 20;
    public static int FIND_NODE_SIZE = 20;
    public static int MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE = 20;

    // V4
    public static int PING_SCHEDULE_TIME_VALUE = 20;
    public static TimeUnit PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
    public static int DHT_EXECUTOR_POOL_SIZE = 20;
    public static int DHT_SCHEDULED_EXECUTOR_POOL_SIZE = 5;
    public static int MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE = 1;
    public static TimeUnit MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT = TimeUnit.MINUTES;
    public static boolean ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = false;

    public static NodeSettings build(){
      return NodeSettings.builder()
              .alpha(ALPHA)
              .identifierSize(IDENTIFIER_SIZE)
              .bucketSize(BUCKET_SIZE)
              .findNodeSize(FIND_NODE_SIZE)
              .maximumLastSeenAgeToConsiderAlive(MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE)
              .pingScheduleTimeUnit(PING_SCHEDULE_TIME_UNIT)
              .pingScheduleTimeValue(PING_SCHEDULE_TIME_VALUE)
              .dhtExecutorPoolSize(DHT_EXECUTOR_POOL_SIZE)
              .dhtScheduledExecutorPoolSize(DHT_SCHEDULED_EXECUTOR_POOL_SIZE)
              .maximumStoreAndLookupTimeoutValue(MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE)
              .maximumStoreAndGetTimeoutTimeUnit(MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT)
              .enabledFirstStoreRequestForcePass(ENABLED_FIRST_STORE_REQUEST_FORCE_PASS)
              .build();
    }
  }
}
