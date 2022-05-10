package mil.usmc.mls2.stratis.common.share.caching;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

public class LocalHazelcastCacheManager extends HazelcastCacheManager {

  public LocalHazelcastCacheManager(HazelcastInstance hazelcastInstance) {
    super(hazelcastInstance);
  }
}
