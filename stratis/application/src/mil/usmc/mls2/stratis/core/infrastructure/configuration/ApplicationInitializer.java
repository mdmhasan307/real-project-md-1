package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;

/**
 * Responsible for execution and coordination of initialization activities.
 */
@Slf4j
@SuppressWarnings("all")
public class ApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    val environment = configurableApplicationContext.getEnvironment();
    log.info("*** ACTIVE PROFILE(S): {}", String.join(",", environment.getActiveProfiles()));
    Arrays.stream(environment.getActiveProfiles()).filter(s -> Profiles.DEV.equalsIgnoreCase(s)).findFirst().ifPresent(s -> processDev(environment));
  }

  private void processDev(ConfigurableEnvironment environment) {
    val developmentInitializer = new DevelopmentInitializer();
    developmentInitializer.initialize(environment);
  }
}

