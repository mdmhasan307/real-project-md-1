package mil.usmc.mls2.stratis.common.share.caching;

import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Application cluster cache manager (legacy implementation)
 * Uses LocalHazelcastCacheManager instead of IntegrationHazelcastCacheManager as legacy does not integrate with MIPS nor supports clustering
 */
@Component
@Profile(Profiles.LEGACY)
@SuppressWarnings("unused")
public class AppCacheManagerLegacy extends DefaultApplicationCacheManager implements AppCacheManager {

  public AppCacheManagerLegacy(LocalHazelcastCacheManager localHazelcastCacheManager, LocalCacheManager localCacheManager) {
    super(localHazelcastCacheManager);
  }
}
