package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
/**
 * This Utils only works for the EntityManager for Stratis database EntityManager, NOT for strat Common database EntityManager
 * If we need something similar for the Strat Common Database we will need to create seperate class, and use the different @Qualifier value.
 */
public class EntityManagerUtils {

  @Qualifier("entityManagerFactory")
  private static EntityManager staticEntityManger;

  @Qualifier("entityManagerFactory")
  private final EntityManager entityManger;

  @PostConstruct
  private void postConstruct() {
    staticEntityManger = entityManger;
  }

  public static void flushAndClear() {
    staticEntityManger.flush();
    staticEntityManger.clear();
  }

  public static void flush() {
    staticEntityManger.flush();
  }

  public static void clear() {
    staticEntityManger.clear();
  }
}
