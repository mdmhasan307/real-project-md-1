package mil.usmc.mls2.stratis.common.domain.model;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class EventRegistry {

  private final List<Event> events = new ArrayList<>();

  public List<Event> drain() {
    val consumption = peek();
    events.clear();
    return consumption;
  }

  public List<Event> peek() {
    return new ArrayList<>(events);
  }

  public void register(Event event) {
    events.add(event);
  }
}