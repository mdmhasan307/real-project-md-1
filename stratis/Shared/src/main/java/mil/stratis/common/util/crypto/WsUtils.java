package mil.stratis.common.util.crypto;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.JNDIUtils;

@Slf4j
public class WsUtils {

  public static final String TRUSTSTORE_PASS_CONFIG = "ws.truststore.password";
  public static final String TRUSTSTORE_PATH_CONFIG = "ws.truststore.path";
  public static final String KEYSTORE_PASS_CONFIG = "ws.keystore.password";
  public static final String KEYSTORE_PATH_CONFIG = "ws.keystore.path";
  public static final String KEYSTORE_ALIAS_CONFIG = "ws.keystore.alias";

  private WsUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String getConfiguration(String name) {
    String configurationKey;
    JNDIUtils lookup = JNDIUtils.getInstance();
    configurationKey = lookup.getProperty(name);
    if (configurationKey != null) {
      if (configurationKey.startsWith("ENC(")) {
        // Return decoded value
        return CustomEncryptor.decrypt(configurationKey.substring(4, configurationKey.length() - 1));
      }
      else {
        return configurationKey;
      }
    }
    throw new RuntimeException(String.format("WSUtils.getConfiguration: no value for context property: %s found", name));
  }
}
