package mil.usmc.mls2.stratis.common.domain.model;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

public abstract class AggregateBase<I extends EntityId> implements Aggregate<I> {

  @Getter(AccessLevel.NONE)
  private final EventRegistry eventRegistry = new EventRegistry();

  @Override
  public final List<Event> drainEvents() { return eventRegistry.drain(); }

  @Override
  public final List<Event> peekEvents() { return eventRegistry.peek(); }

  protected final void registerEvent(Event event) {
    eventRegistry.register(event);
  }
}