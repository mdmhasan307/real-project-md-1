package mil.usmc.mls2.stratis.core.utility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchOperator {

  AND(0, "And", " AND "),
  OR(1, "Or", " OR ");

  private final int id;
  private final String label;
  private final String sql;
}