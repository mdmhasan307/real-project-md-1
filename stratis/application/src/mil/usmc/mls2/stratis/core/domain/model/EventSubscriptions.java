package mil.usmc.mls2.stratis.core.domain.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The EventSubscriptions is a class to hold details on current event subscribers,
 * by their name and camel routing destination.  These subscribers are held in the
 * internal HashMap and this object is held in the system cache.  The object
 * should not expire from cache, and in a clustered configuration, would be maintained
 * consistently between all cache nodes.
 */
public class EventSubscriptions implements Serializable {

  private final Map<String, EventSubscription> eventSubscriptions = new HashMap<>();

  public void addSubscription(EventSubscription eventSubscription) {
    if (eventSubscription == null) {
      return;
    }

    eventSubscriptions.put(eventSubscription.getName(), eventSubscription);
  }

  public void removeSubscription(EventSubscription eventSubscription) {
    if (eventSubscription == null) {
      return;
    }

    eventSubscriptions.remove(eventSubscription.getName());
  }

  /**
   * Returns subscription destinations
   *
   * @return collection of destinations
   */
  public Collection<String> getSubscriptions() {
    return eventSubscriptions.values().stream().map(EventSubscription::getEndpoint).collect(Collectors.toList());
  }
}
