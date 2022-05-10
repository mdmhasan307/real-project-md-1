package mil.usmc.mls2.stratis.common.domain.model;

import java.util.List;

/**
 * Aggregate Root
 */
public interface Aggregate<I extends EntityId> extends Entity<I> {

  List<Event> drainEvents();

  List<Event> peekEvents();
}
