package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSiteSelectionTracker {

  private static ThreadLocal<String> siteSelection = new ThreadLocal<>();
  private static ThreadLocal<String> siteName = new ThreadLocal<>();

  public static void configureTracker(String userSiteSelection, String siteNameOfSelection) {
    siteSelection.set(userSiteSelection);
    siteName.set(siteNameOfSelection);
  }

  public static void clear() {
    siteSelection.remove();
    siteName.remove();
  }

  public static String getUserSiteSelection() {
    return siteSelection.get();
  }

  public static String getSiteName() { return siteName.get(); }
}
