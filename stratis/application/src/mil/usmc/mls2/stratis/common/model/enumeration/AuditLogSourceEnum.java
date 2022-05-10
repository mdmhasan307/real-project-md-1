package mil.usmc.mls2.stratis.common.model.enumeration;

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.List;

public enum AuditLogSourceEnum implements PersistableEnum<Integer, String> {

  SECURITY(1, "Security"),
  APPLICATION(2, "Application");

  private final Integer id;
  private final String label;

  /**
   * A static list containing all elements from this enumeration.
   */
  public static final List<KeyValue<String, String>> KEY_VALUE_LIST;

  static {
    final ImmutableList.Builder<KeyValue<String, String>> builder = ImmutableList.builder();
    for (AuditLogSourceEnum statusEnum : values()) {
      builder.add(new DefaultKeyValue<>(statusEnum.name(), statusEnum.getLabel()));
    }
    KEY_VALUE_LIST = builder.build();
  }

  // constructor
  AuditLogSourceEnum(Integer id, String label) {
    this.id = id;
    this.label = label;
  }

  public Integer getId() {
    return id;
  }

  // the AuditLogCategoryEnum unique Id value.
  public String getLabel() {
    return this.label;
  }

  public static AuditLogSourceEnum getType(final String label) {
    for (AuditLogSourceEnum type : AuditLogSourceEnum.values()) {
      if (type.label.equalsIgnoreCase(label)) {
        return type;
      }
    }

    return null;
  }
}
