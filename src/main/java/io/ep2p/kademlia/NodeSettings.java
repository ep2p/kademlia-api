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
// TODO: remove unused when V4 done
public class NodeSettings implements Serializable {
  public long bootstrapNodeCallTimeout;
  public long storeTimeout;
  public int alpha;
  public int identifierSize;
  public int referencedNodesUpdatePeriod;
  /* Maximum size of the buckets */
  public int bucketSize;
  public int findNodeSize;
  public int joinBucketQueries;
  public int maximumLastSeenAgeToConsiderAlive;
  public boolean enabledRepublishing;
  public RepublishSettings republishSettings;


  // V4
  public int pingScheduleTimeValue;
  public TimeUnit pingScheduleTimeUnit;
  public int storeRequestPoolSize;
  public int storeAndGetCleanupPoolSize;
  public int maximumStoreAndLookupTimeoutValue;
  public TimeUnit maximumStoreAndGetTimeoutTimeUnit;


  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RepublishSettings {
    private long republishIntervalValue = 30;
    private TimeUnit republishIntervalUnit = TimeUnit.MINUTES;
    private int republishQueryTimeValue = 1;
    private TimeUnit republishQueryUnit = TimeUnit.HOURS;
    private int republishQuerySizePerPage = 20;
  }

  public static class Default {
    public static long BOOTSTRAP_NODE_CALL_TIMEOUT = 100;
    public static long STORE_TIMEOUT = 20;
    public static int ALPHA = 10;
    public static int IDENTIFIER_SIZE = 128;
    public static int REFERENCED_NODES_UPDATE_PERIOD = 30;
    public static int BUCKET_SIZE = 20;
    public static int FIND_NODE_SIZE = 20;
    public static int JOIN_BUCKET_QUERIES = 1;
    public static int MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE = 20;

    // V4
    public static int PING_SCHEDULE_TIME_VALUE = 20;
    public static TimeUnit PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
    public static int STORE_REQUEST_POOL_SIZE = 20;
    public static int STORE_AND_GET_CLEANUP_POOL_SIZE = 40;
    public static int MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE = 1;
    public static TimeUnit MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT = TimeUnit.MINUTES;


    public static boolean ENABLED_KEY_REPUBLISHING = false;
    public static RepublishSettings REPUBLISH_SETTINGS;

    public static NodeSettings build(){
      return NodeSettings.builder()
              .bootstrapNodeCallTimeout(BOOTSTRAP_NODE_CALL_TIMEOUT)
              .storeTimeout(STORE_TIMEOUT)
              .alpha(ALPHA)
              .identifierSize(IDENTIFIER_SIZE)
              .referencedNodesUpdatePeriod(REFERENCED_NODES_UPDATE_PERIOD)
              .bucketSize(BUCKET_SIZE)
              .findNodeSize(FIND_NODE_SIZE)
              .joinBucketQueries(JOIN_BUCKET_QUERIES)
              .maximumLastSeenAgeToConsiderAlive(MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE)
              .pingScheduleTimeUnit(PING_SCHEDULE_TIME_UNIT)
              .pingScheduleTimeValue(PING_SCHEDULE_TIME_VALUE)
              .storeRequestPoolSize(STORE_REQUEST_POOL_SIZE)
              .storeAndGetCleanupPoolSize(STORE_AND_GET_CLEANUP_POOL_SIZE)
              .maximumStoreAndLookupTimeoutValue(MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE)
              .maximumStoreAndGetTimeoutTimeUnit(MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT)
              .enabledRepublishing(ENABLED_KEY_REPUBLISHING)
              .republishSettings(ENABLED_KEY_REPUBLISHING ? REPUBLISH_SETTINGS : null)
              .build();
    }
  }
}
