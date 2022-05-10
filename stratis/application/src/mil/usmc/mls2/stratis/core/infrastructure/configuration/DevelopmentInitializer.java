package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Configuration for development environments
 */
@Slf4j
public class DevelopmentInitializer {

  public void initialize(ConfigurableEnvironment environment) {
    log.info("*** initializing development environment...");
  }
}
