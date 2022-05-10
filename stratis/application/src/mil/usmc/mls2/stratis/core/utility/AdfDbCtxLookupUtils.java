package mil.usmc.mls2.stratis.core.utility;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdfDbCtxLookupUtils {

  //FUTURE post innov 1.0 everything needs to use the legacy path for now.  unable to get ADF working with DB connections in non standard java:comp/env/jdbc location
  public static final String PATH = "stratis/jdbc/";
  public static final String LEGACY_PATH = "java:comp/env/jdbc/";
  private final StratisConfig multiTenancyConfig;

  public String getDbCtxLookupPath() {
    boolean multiTenancyEnabled = multiTenancyConfig.isMultiTenancyEnabled();

    if (multiTenancyEnabled) {
      String dbPoolForUser = UserSiteSelectionTracker.getUserSiteSelection();
      return LEGACY_PATH + dbPoolForUser;
    }
    return LEGACY_PATH + GlobalConstants.LEGACY_DATABASE_CONNECTION_NAME;
  }

  public String getPathWithoutDB() {
    boolean multiTenancyEnabled = multiTenancyConfig.isMultiTenancyEnabled();

    if (multiTenancyEnabled) {
      return LEGACY_PATH;
    }
    return LEGACY_PATH;
  }
}
