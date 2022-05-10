package mil.usmc.mls2.stratis.common.model.enumeration;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * An enumeration type defining the various possible search types within the system.
 */
public enum SearchTypeEnum {
  /**
   * Defines an "and" search in which filter criteria are connected with the "AND" operator.  Each filter value
   * should be present in a matched row.
   */
  AND(0, "And", " AND "),

  /**
   * Defines an "or" search in which filter criteria are connected with the "OR" operator.  Only one filter value
   * needs to be present in a matched row.
   */
  OR(1, "Or", " OR "),

  /**
   * Defines a "Strict" search in which all filter criteria must be matched exactly.
   */
  STRICT(2, "Strict", " AND ");

  /**
   * Numerical identifier uniquely describing the search type.
   */
  private final int id;

  /**
   * A textual label for the search type.
   */
  private final String label;

  /**
   * The SQL string used to join search filters.
   */
  private final String sql;

  /**
   * A static list containing all elements from this enumeration.
   */
  public static final List<SearchTypeEnum> LIST = ImmutableList.copyOf(values());

  /**
   * @param id    Type's id value.
   * @param label Type's label value.
   * @param sql   The SQL string that will be used to join search filters.
   */
  SearchTypeEnum(int id, String label, String sql) {
    this.id = id;
    this.label = label;
    this.sql = sql;
  }

  /**
   * Return the id associated with this enumeration type.
   *
   * @return The id associated with this enumeration type.
   */
  public int getId() {
    return this.id;
  }

  /**
   * Return the label String associated with this enumeration type.
   *
   * @return The label String associated with this enumeration type.
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * Return the SQL String associated with this enumeration type - used to join multiple filter values.
   *
   * @return The SQL String associated with this enumeration type.
   */
  public String getSql() {
    return this.sql;
  }

  /**
   * Return the SearchTypeEnum for the passed id.
   *
   * @param id Id value.
   * @return Associated SearchTypeEnum, or <code>null</code> if the id value is not found.
   */
  public static SearchTypeEnum getType(int id) {
    for (SearchTypeEnum type : SearchTypeEnum.values()) {
      if (type.id == id) {
        return type;
      }
    }

    return null;
  }

  /**
   * Return the SearchTypeEnum for the passed label.
   *
   * @param label Label value.
   * @return Associated SearchTypeEnum, or <code>null</code> if the label value is not found.
   */
  public static SearchTypeEnum getType(String label) {
    for (SearchTypeEnum type : SearchTypeEnum.values()) {
      if (type.label.equalsIgnoreCase(label)) {
        return type;
      }
    }

    return null;
  }
}