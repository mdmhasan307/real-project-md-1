package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanConstants {

  public static final String APP_HAZELCAST_INSTANCE = "appHazelcastInstance";
  public static final String APP_HAZELCAST_CACHE_MANAGER = "appHazelcastCacheManager";
  public static final String MLS2_SHARE_HAZELCAST_INSTANCE = "mls2ShareHazelcastInstance";
  public static final String MLS2_SHARE_HAZELCAST_CACHE_MANGER = "mls2ShareHazelcastCacheManager";
  public static final String INTEGRATION_JMS = "integration";
  public static final String INTEGRATION_JMS_CONNECTION_FACTORY = "integrationJmsConnectionFactory";
  public static final String INTEGRATION_JMS_CONFIGURATION = "integrationJmsConfiguration";
}
