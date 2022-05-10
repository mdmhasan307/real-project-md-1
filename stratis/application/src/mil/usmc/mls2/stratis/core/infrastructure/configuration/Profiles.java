package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Profiles {

  public static final String DEV = "dev";
  public static final String TEST = "test";
  public static final String QA = "qa";
  public static final String STAGING = "staging";
  public static final String PROD = "prod";

  public static final String INTEGRATION_ENTERPRISE = "integration_enterprise";
  public static final String INTEGRATION_TACTICAL = "integration_tactical";
  public static final String INTEGRATION_ANY = "integration | integration_tactical | integration_enterprise";
  public static final String LEGACY = "!integration & !integration_tactical & !integration_enterprise";

  public static boolean isIntegrationAny(Environment environment) {
    return isEnterpriseMode(environment) || isTacticalMode(environment);
  }

  public static boolean isActive(String profile, Environment environment) {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase(profile));
  }

  public static boolean isNotActive(String profile, Environment environment) {
    return !isActive(profile, environment);
  }

  public static boolean isEnterpriseMode(Environment environment) {
    return isActive(INTEGRATION_ENTERPRISE, environment);
  }

  public static boolean isTacticalMode(Environment environment) {
    return isActive(INTEGRATION_TACTICAL, environment);
  }

  public static boolean isNotEnterpriseMode(Environment environment) {
    return isNotActive(INTEGRATION_ENTERPRISE, environment);
  }

  public static boolean isNotTacticalMode(Environment environment) {
    return isNotActive(INTEGRATION_TACTICAL, environment);
  }

  public static boolean isLegacyMode(Environment environment) {
    return isActive(LEGACY, environment);
  }

  public static boolean isDevelopment(Environment environment) {return isActive(DEV, environment);}

  public static boolean isProduction(Environment environment) {return isActive(PROD, environment);}
}
