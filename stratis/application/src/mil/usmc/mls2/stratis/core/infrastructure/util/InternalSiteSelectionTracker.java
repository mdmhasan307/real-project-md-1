package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalSiteSelectionTracker {

  private static ThreadLocal<String> siteSelection = new ThreadLocal<>();

  public static void configureTracker(String userSiteSelection) {
    siteSelection.set(userSiteSelection);
  }

  public static void clear() {
    siteSelection.remove();
  }

  public static String getSiteSelection() {
    return siteSelection.get();
  }
}
