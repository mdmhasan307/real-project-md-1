package mil.usmc.mls2.stratis.core.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Transitional class used to support cases where static access to context elements are needed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContextUtils {

  private final ApplicationContext applicationContext;

  private static ApplicationContext staticApplicationContext;

  @PostConstruct
  @SuppressWarnings("squid:S2696")
  private void postConstruct() {
    staticApplicationContext = applicationContext;
  }

  public static <T> T getBean(Class<T> clazz) {
    return staticApplicationContext.getBean(clazz);
  }
}
