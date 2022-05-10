package mil.usmc.mls2.stratis.common.utility;

import mil.usmc.mls2.stratis.common.model.enumeration.PersistableEnum;

import javax.persistence.AttributeConverter;

/**
 * This will take the String (Label) value out of the PersistableEnum to store in the database. Likewise it will take the value from the DB
 * and find the appropriate Enum with the matching Label and return it.
 */
public class AbstractEnumConverter<T extends Enum<T> & PersistableEnum<Integer, String>> implements AttributeConverter<T, String> {

  private final Class<T> clazz;

  public AbstractEnumConverter(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public String convertToDatabaseColumn(T attribute) {
    return attribute != null ? attribute.getLabel() : null;
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    T[] enums = clazz.getEnumConstants();

    for (T e : enums) {
      if (e.getLabel().equals(dbData)) {
        return e;
      }
    }

    throw new UnsupportedOperationException();
  }
}