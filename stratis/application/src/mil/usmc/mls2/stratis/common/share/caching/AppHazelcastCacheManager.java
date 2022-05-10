package mil.usmc.mls2.stratis.common.share.caching;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

/**
 * This class is primarily intended to define the default cache manager for the application (via IntegrationConfig)
 */
public class AppHazelcastCacheManager extends HazelcastCacheManager {

  public AppHazelcastCacheManager(HazelcastInstance hazelcastInstance) {
    super(hazelcastInstance);
  }
}
