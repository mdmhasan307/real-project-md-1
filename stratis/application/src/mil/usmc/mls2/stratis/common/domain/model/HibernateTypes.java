package mil.usmc.mls2.stratis.common.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Represents Hibernate-provided entity types
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HibernateTypes {

  public static final String UUID_BINARY = "uuid-binary"; // <== this is an actual built-in hibernate type
  public static final String UUID_CHAR = "uuid-char"; // <== this is an actual built-in hibernate type
  public static final String YES_NO = "yes_no";
}