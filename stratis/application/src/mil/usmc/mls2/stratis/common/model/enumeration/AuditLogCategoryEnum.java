package mil.usmc.mls2.stratis.common.model.enumeration;

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.List;

public enum AuditLogCategoryEnum implements PersistableEnum<Integer, String> {

  AUTH_FAIL(1, "Authentication Failure"),
  AUTH_SUCCESS(2, "Authentication Success"),
  AUTH_LOGOUT(3, "Authentication Log Out"),
  AUTH_FORCE_LOGOUT(4, "Authentication Force Log Out"),
  AUTH_ACCOUNT_LOCKED(5, "Authentication Account Locked"),
  AUTH_ACCOUNT_UNLOCKED(6, "Authentication Account Unlocked"),
  AUTH_WORKSTATION_SWITCHED(7, "Authentication User Switched Workstation"),
  AUTH_LOGIN_FAILED(8, "Authentication Login Failed"),
  AUTH_ACCOUNT_EXPIRED(9, "Authentication Account Expired"),
  SESSION_TIMEOUT(10, "Session Timeout"),
  USER_TYPE_DELETED(11, "User Type Deleted"),
  USER_GROUP_DELETED(12, "User Group Deleted"),
  GROUP_DELETED_SUCCESS(13, "Group Deleted Successfully"),
  GROUP_DELETED_FAIL(14, "Group Delete Failure"),
  USER_TYPE_ADDED(15, "User Type Added"),
  USER_GROUP_ADDED(16, "User Group Added"),
  USER_PERMISSION_ERROR(17, "User Permission Error"),
  ACTION_UPDATE_CUSTOMER(18, "Customer Modified"),
  ACTION_CHANGE_LOCATION(19, "Location Changed"),
  USER_GROUP_PRIVILEGE_ADDED(18, "User Group Privilege Added"),
  USER_GROUP_PRIVILEGE_DELETED(19, "User Group Privilege Deleted"),
  ADMIN_PAGE_ACCESS(21, "Admin Page Access"),
  ADMIN_PAGE_UNAUTHORIZED_ACCESS(22, "Admin Page Unauthorized Access");

  private final Integer id;
  private final String label;

  /**
   * A static list containing all elements from this enumeration.
   */
  public static final List<KeyValue<String, String>> KEY_VALUE_LIST;

  static {
    final ImmutableList.Builder<KeyValue<String, String>> builder = ImmutableList.builder();
    for (AuditLogCategoryEnum statusEnum : values()) {
      builder.add(new DefaultKeyValue<>(statusEnum.name(), statusEnum.getLabel()));
    }
    KEY_VALUE_LIST = builder.build();
  }

  AuditLogCategoryEnum(Integer id, String label) {
    this.id = id;
    this.label = label;
  }

  public Integer getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  // return the AuditLogCategoryEnum for the passed label.
  public static AuditLogCategoryEnum getType(final String label) {
    for (AuditLogCategoryEnum type : AuditLogCategoryEnum.values()) {
      if (type.label.equalsIgnoreCase(label)) {
        return type;
      }
    }

    return null;
  }
}
