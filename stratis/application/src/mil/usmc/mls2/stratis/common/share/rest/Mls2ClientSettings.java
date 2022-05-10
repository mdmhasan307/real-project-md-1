package mil.usmc.mls2.stratis.common.share.rest;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * General Client connections from MIPS to other locally integrated MLS2 systems
 * <p>
 * Design:
 * Configuration properties are delegated from a nested object to circumvent a limitation of spring @ConstructorBinding
 * as of spring boot 2.3.4 / spring 5.2.9 - @ConstructorBinding and @Component cannot be mixed
 */
@Component
@RequiredArgsConstructor
public class Mls2ClientSettings implements RestClientSettings {

  @Delegate
  private final Properties properties;

  @Value
  @Accessors(fluent = true)
  @ConstructorBinding
  @ConfigurationProperties(prefix = "stratis.integration.mls2.default.client")
  @ToString(exclude = {"keyStorePass", "trustStorePass"})
  static class Properties {

    int connectTimeoutSeconds;
    int readTimeoutSeconds;
    boolean keyMaterialEnabled;
    boolean trustMaterialTrustAll;
    String keyStoreFile;
    String keyStoreType;
    String keyStorePass;
    String keyStoreAlias;
    String trustStoreFile;
    String trustStoreType;
    String trustStorePass;

    /**
     * Constructor manually created to control constructor argument names in support of meta-inf information for IDEs,
     * otherwise parameters naming pattern is arg0...argN
     */
    public Properties(
        int connectTimeoutSeconds,
        int readTimeoutSeconds,
        boolean keyMaterialEnabled,
        boolean trustMaterialTrustAll,
        String keyStoreFile,
        String keyStoreType,
        String keyStorePass,
        String keyStoreAlias,
        String trustStoreFile,
        String trustStoreType,
        String trustStorePass
    ) {
      this.connectTimeoutSeconds = connectTimeoutSeconds;
      this.readTimeoutSeconds = readTimeoutSeconds;
      this.keyMaterialEnabled = keyMaterialEnabled;
      this.trustMaterialTrustAll = trustMaterialTrustAll;
      this.keyStoreFile = keyStoreFile;
      this.keyStoreType = keyStoreType;
      this.keyStorePass = keyStorePass;
      this.keyStoreAlias = keyStoreAlias;
      this.trustStoreFile = trustStoreFile;
      this.trustStoreType = trustStoreType;
      this.trustStorePass = trustStorePass;
    }
  }
}
