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

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RepublishSettings {
    public int republishIntervalValue = 30;
    public TimeUnit republishIntervalUnit = TimeUnit.MINUTES;
    public int republishQueryValue = 1;
    public TimeUnit republishQueryUnit = TimeUnit.HOURS;
    public int republishQuerySize = 100;
  }

  public static class Default {
    public static long BOOTSTRAP_NODE_CALL_TIMEOUT = 100;
    public static long STORE_TIMEOUT = 20;
    public static int ALPHA = 3;
    public static int IDENTIFIER_SIZE = 128;
    public static int REFERENCED_NODES_UPDATE_PERIOD = 30;
    public static int BUCKET_SIZE = 20;
    public static int FIND_NODE_SIZE = 20;
    public static int JOIN_BUCKET_QUERIES = 1;
    public static int MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE = 20;
    public static boolean ENABLED_KEY_REPUBLISHING = false;
    public static RepublishSettings REPUBLISH_SETTINGS = new RepublishSettings();

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
              .enabledRepublishing(ENABLED_KEY_REPUBLISHING)
              .republishSettings(ENABLED_KEY_REPUBLISHING ? REPUBLISH_SETTINGS : null)
              .build();
    }
  }
}
