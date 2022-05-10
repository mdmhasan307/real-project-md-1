package mil.usmc.mls2.stratis.common.share.caching;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

public class Mls2HazelcastCacheManager extends HazelcastCacheManager {

  public Mls2HazelcastCacheManager(HazelcastInstance hazelcastInstance) {
    super(hazelcastInstance);
  }
}
