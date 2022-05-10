package mil.usmc.mls2.stratis.modules.workload.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PackingStationType {
  INVALID("Invalid"),
  SINGLE("Packing Station - Single"),
  CONSOLIDATION("Packing Station - Consolidation");

  private final String stationName;

  public boolean isValid() {
    return this != INVALID;
  }

  public boolean isConsolidated() {
    return this == CONSOLIDATION;
  }

  public boolean isSingle() {
    return this == SINGLE;
  }
}
