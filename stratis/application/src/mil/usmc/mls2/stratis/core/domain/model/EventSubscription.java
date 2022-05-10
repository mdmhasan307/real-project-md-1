package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * An EventSubscription is a class that represents a single instance of an event subscriber, based on name, and
 * camel routing information.
 */
@Getter
@RequiredArgsConstructor(staticName = "of")
@ToString
public class EventSubscription implements Serializable {

  private final String name;
  private final String endpoint;

  public static EventSubscription of(Class<?> clazz, String endpoint) {
    return EventSubscription.of(clazz.getName(), endpoint);
  }
}
