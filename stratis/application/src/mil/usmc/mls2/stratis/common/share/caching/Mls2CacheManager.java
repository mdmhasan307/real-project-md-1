package mil.usmc.mls2.stratis.common.share.caching;

import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * MLS2 cache manager (cache shared across MLS2 systems)
 */
@Component
@Profile(Profiles.INTEGRATION_ANY)
public class Mls2CacheManager extends DefaultApplicationCacheManager {

  public Mls2CacheManager(Mls2HazelcastCacheManager mls2HazelcastCacheManager) {
    super(mls2HazelcastCacheManager);
  }
}
