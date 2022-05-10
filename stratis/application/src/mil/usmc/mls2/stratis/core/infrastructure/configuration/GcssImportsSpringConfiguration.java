package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GcssImportsSpringConfiguration {

  @Bean("GCSS_IMPORTS_TXR")
  public GcssImportsTaskExecutor gcssImportsTaskExecutor() {
    val executor = new GcssImportsTaskExecutor();
    executor.setThreadNamePrefix("gcss-imports-txr");
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(1);
    executor.setDaemon(true);
    executor.initialize();

    return executor;
  }
}
