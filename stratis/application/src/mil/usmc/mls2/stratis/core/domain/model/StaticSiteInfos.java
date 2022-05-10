package mil.usmc.mls2.stratis.core.domain.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.infrastructure.util.InternalSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Note: This class is to support a one time load of data from the SITE_INFO table that never changes.  To make quick
 * references without an extra DB hit every time to access.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class StaticSiteInfos {

  private boolean initialized;
  private final StratisConfig stratisConfig;
  private final DefaultTransactionExecutor defaultTransactionExecutor;
  private final SiteInfoService siteInfoService;

  private final HashMap<String, StaticSiteInfo> siteInfos = new HashMap<>();

  private void addSiteInfo(String dbConnection, StaticSiteInfo staticSiteInfo) {
    siteInfos.put(dbConnection, staticSiteInfo);
  }

  public StaticSiteInfo getSiteInfo(String dbConnection) {
    checkInitialized();
    return siteInfos.get(dbConnection);
  }

  private void checkInitialized() {
    if (!initialized) throw new IllegalStateException("Site Info not Initalized");
  }

  @EventListener
  @SuppressWarnings("unused")
  public void onApplicationReady(ApplicationReadyEvent event) {
    if (stratisConfig.isMultiTenancyEnabled()) {
      if (CollectionUtils.isNotEmpty(stratisConfig.getDatasources())) {
        stratisConfig.getDatasources().forEach(datasourceName -> {
          InternalSiteSelectionTracker.configureTracker(datasourceName);

          Runnable loadSiteInfo = () -> {
            val siteInfo = siteInfoService.getStaticRecord();
            addSiteInfo(datasourceName, siteInfo);
          };

          defaultTransactionExecutor.execute("loadSiteInfo", loadSiteInfo, e -> {
            throw new StratisRuntimeException(String.format("Error loading Site Info for %s", datasourceName), null, e);
          });
        });
      }
      else {
        log.info("No sites are configured and active.  Static Site Infos not loaded");
      }
    }
    else {
      val siteInfo = siteInfoService.getStaticRecord();
      addSiteInfo(GlobalConstants.LEGACY_DATABASE_CONNECTION_NAME, siteInfo);
    }
    initialized = true;
  }
}




