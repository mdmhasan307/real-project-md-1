package mil.usmc.mls2.stratis.common.share.caching;

import org.springframework.stereotype.Component;

/**
 * Application local cache manager (non-clustered, non-distributed)
 */
@Component
@SuppressWarnings("unused")
public class LocalCacheManager extends DefaultApplicationCacheManager {

  public LocalCacheManager(LocalHazelcastCacheManager localHazelcastCacheManager) {
    super(localHazelcastCacheManager);
  }
}