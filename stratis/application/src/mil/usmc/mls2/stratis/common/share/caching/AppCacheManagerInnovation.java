package mil.usmc.mls2.stratis.common.share.caching;

import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Application cluster cache manager
 */
@Component
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("unused")
public class AppCacheManagerInnovation extends DefaultApplicationCacheManager implements AppCacheManager {

  public AppCacheManagerInnovation(AppHazelcastCacheManager appHazelcastCacheManager) {
    super(appHazelcastCacheManager);
  }
}
