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
  public int identifierSize;
  /* Maximum size of the buckets */
  public int bucketSize;
  public int findNodeSize;
  public int maximumLastSeenAgeToConsiderAlive;

  public int pingScheduleTimeValue;
  @Builder.Default
  public TimeUnit pingScheduleTimeUnit = TimeUnit.SECONDS;
  public int dhtExecutorPoolSize;
  public int scheduledExecutorPoolSize;
  public boolean enabledFirstStoreRequestForcePass;


  public static class Default {
    public static int IDENTIFIER_SIZE = 128;
    public static int BUCKET_SIZE = 20;
    public static int FIND_NODE_SIZE = 20;
    public static int MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE = 20;

    // V4
    public static int PING_SCHEDULE_TIME_VALUE = 20;
    public static TimeUnit PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
    public static int DHT_EXECUTOR_POOL_SIZE = 20;
    public static int SCHEDULED_EXECUTOR_POOL_SIZE = 1;
    public static boolean ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = false;

    public static NodeSettings build(){
      return NodeSettings.builder()
              .identifierSize(IDENTIFIER_SIZE)
              .bucketSize(BUCKET_SIZE)
              .findNodeSize(FIND_NODE_SIZE)
              .maximumLastSeenAgeToConsiderAlive(MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE)
              .pingScheduleTimeUnit(PING_SCHEDULE_TIME_UNIT)
              .pingScheduleTimeValue(PING_SCHEDULE_TIME_VALUE)
              .dhtExecutorPoolSize(DHT_EXECUTOR_POOL_SIZE)
              .scheduledExecutorPoolSize(SCHEDULED_EXECUTOR_POOL_SIZE)
              .enabledFirstStoreRequestForcePass(ENABLED_FIRST_STORE_REQUEST_FORCE_PASS)
              .build();
    }
  }
}
