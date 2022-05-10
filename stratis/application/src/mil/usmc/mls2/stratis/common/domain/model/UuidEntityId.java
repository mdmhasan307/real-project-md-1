package mil.usmc.mls2.stratis.common.domain.model;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "uuid")
public class UuidEntityId implements EntityId {

  @NonNull
  private final UUID uuid;

  @Override
  public final UUID toUuid() {return uuid; }

  @Override
  public final String toString() {
    return uuid.toString();
  }
}