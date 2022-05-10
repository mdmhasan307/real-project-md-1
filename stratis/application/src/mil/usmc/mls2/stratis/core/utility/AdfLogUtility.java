package mil.usmc.mls2.stratis.core.utility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdfLogUtility {

  public static void logException(Throwable e) {
    log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
  }

  public static void logException(Exception e) {
    log.error("Error in {}.{}", e.getStackTrace()[0].getClassName(), e.getStackTrace()[0].getMethodName(), e);
  }

  public static void logExceptionAsWarning(Exception e) {
    log.warn("Warning in {}", e.getStackTrace()[0].getMethodName(), e);
  }
}
