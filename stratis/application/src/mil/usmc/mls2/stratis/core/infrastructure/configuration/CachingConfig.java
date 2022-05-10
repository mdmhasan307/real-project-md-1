package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.common.share.caching.AppHazelcastCacheManager;
import mil.usmc.mls2.stratis.common.share.caching.Mls2HazelcastCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Integration caching client configuration
 * <p>
 * Implementation:
 * - Hazelcast client of MIPS middleware caching services
 * - configuration will be leveraged by various cache managers (mls2-share, mips-app)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
@ConditionalOnProperty(prefix = "spring.jta", name = "enabled", havingValue = "false")
public class CachingConfig {

  private final AppProperties appProps;
  private final CachingConfigProps props;

  @Bean(name = BeanConstants.APP_HAZELCAST_INSTANCE, destroyMethod = "shutdown")
  public HazelcastInstance appHazelcastInstance() {
    return CachingConfigHelper.createHazelcastInstance(props.getClusters().getApp(), props, appProps);
  }

  @Primary
  @Bean(name = BeanConstants.APP_HAZELCAST_CACHE_MANAGER)
  public AppHazelcastCacheManager appHazelcastCacheManager() {
    return new AppHazelcastCacheManager(appHazelcastInstance());
  }

  @Bean(name = BeanConstants.MLS2_SHARE_HAZELCAST_INSTANCE, destroyMethod = "shutdown")
  public HazelcastInstance mls2HazelcastInstance() {
    return CachingConfigHelper.createHazelcastInstance(props.getClusters().getMls2Share(), props, appProps);
  }

  @Bean(name = BeanConstants.MLS2_SHARE_HAZELCAST_CACHE_MANGER)
  public Mls2HazelcastCacheManager mls2HazelcastCacheManager() {
    return new Mls2HazelcastCacheManager(mls2HazelcastInstance());
  }
}
