package mil.usmc.mls2.stratis.common.domain.model;

import java.util.UUID;

public interface EntityId extends ValueObject {

  UUID toUuid();
}