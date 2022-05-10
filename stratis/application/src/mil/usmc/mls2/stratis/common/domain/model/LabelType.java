package mil.usmc.mls2.stratis.common.domain.model;

import org.apache.commons.lang3.StringUtils;

public enum LabelType {
  NONE,
  STOW_LABEL,
  PICK_LABEL,
  NIIN_LABEL,
  LOCATION_LABEL;

  public static LabelType safeValueOf(String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }

    return valueOf(StringUtils.upperCase(StringUtils.trim(value)));
  }
}