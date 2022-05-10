package mil.usmc.mls2.stratis.common.domain.model;

import org.apache.commons.lang3.StringUtils;

public enum SortOrder {
  ASC,
  DESC;

  public static SortOrder safeValueOf(String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }

    return valueOf(StringUtils.upperCase(StringUtils.trim(value)));
  }
}

