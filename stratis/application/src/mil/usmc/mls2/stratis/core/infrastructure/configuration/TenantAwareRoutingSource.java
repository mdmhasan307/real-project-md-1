package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.util.InternalSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class TenantAwareRoutingSource extends AbstractRoutingDataSource {
  
  @Override
  protected Object determineCurrentLookupKey() {
    String dbPoolSelection = UserSiteSelectionTracker.getUserSiteSelection();

    if (dbPoolSelection == null) {
      dbPoolSelection = InternalSiteSelectionTracker.getSiteSelection();
    }
    return dbPoolSelection;
  }
}
