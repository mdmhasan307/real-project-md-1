package mil.usmc.mls2.stratis.core.domain.model;

import com.google.common.collect.ImmutableList;
import mil.usmc.mls2.stratis.common.model.enumeration.PersistableEnum;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.List;

public enum SiteStatusEnum implements PersistableEnum<Integer, String> {

  EMPTY(0, "Empty"),
  PRIMED(1, "Primed"),
  ACTIVE(5, "Active");

  private final Integer id;
  private final String label;

  /**
   * A static list containing all elements from this enumeration.
   */
  public static final List<KeyValue<String, String>> KEY_VALUE_LIST;

  static {
    final ImmutableList.Builder<KeyValue<String, String>> builder = ImmutableList.builder();
    for (SiteStatusEnum statusEnum : values()) {
      builder.add(new DefaultKeyValue<>(statusEnum.name(), statusEnum.getLabel()));
    }
    KEY_VALUE_LIST = builder.build();
  }

  SiteStatusEnum(Integer id, String label) {
    this.id = id;
    this.label = label;
  }

  public Integer getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  // return the SiteStatusEnum for the passed label.
  public static SiteStatusEnum getType(final Integer id) {
    for (SiteStatusEnum type : SiteStatusEnum.values()) {
      if (type.id.equals(id)) {
        return type;
      }
    }

    return null;
  }
}
