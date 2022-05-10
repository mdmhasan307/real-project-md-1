package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.share.caching.AppCacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class StratisConfigInnovationImpl implements StratisConfig {

  private static final String DATA_SOURCE_SET = "DATA_SOURCE_SET";
  private static final String SITE_LIST = "SITE_LIST";

  private final AppCacheManager appCacheManager;

  //Whenever integration is enabled, multi tenancy is Enabled, but its still possible to only
  //have One database configured in a multi-tenant environment.
  public boolean isMultiTenancyEnabled() {
    return true;
  }

  //If there is more then one datasource go ahead and display the site name.
  public boolean isDisplayCurrentSiteName() {
    val dataSources = getDatasources();
    return dataSources.size() > 1;
  }

  public List<String> getDatasources() {
    val hmap = appCacheManager.getMap(DATA_SOURCE_SET);
    return (List<String>) hmap.get(SITE_LIST);
  }

  public void setDatasources(List<String> sites) {
    val hmap = appCacheManager.getMap(DATA_SOURCE_SET);
    hmap.set(SITE_LIST, sites);
  }
}
