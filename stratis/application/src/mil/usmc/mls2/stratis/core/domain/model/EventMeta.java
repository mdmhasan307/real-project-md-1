package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class EventMeta implements Serializable {

  private final UUID eventId;
  private final String label;
  private final OffsetDateTime timestamp;

  public static EventMeta of(String label) {
    return EventMeta.of(UUID.randomUUID(), label, OffsetDateTime.now());
  }
}
