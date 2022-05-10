package mil.usmc.mls2.stratis.common.model.converter;

import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogCategoryEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogSourceEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.common.utility.AbstractEnumConverter;

import javax.persistence.Converter;

public class EnumConverters {

  @Converter(autoApply = true)
  public static class AuditLotTypeConverter extends AbstractEnumConverter<AuditLogTypeEnum> {

    public AuditLotTypeConverter() {
      super(AuditLogTypeEnum.class);
    }
  }

  @Converter(autoApply = true)
  public static class AuditLotSourceConverter extends AbstractEnumConverter<AuditLogSourceEnum> {

    public AuditLotSourceConverter() {
      super(AuditLogSourceEnum.class);
    }
  }

  @Converter(autoApply = true)
  public static class AuditLotCategoryConverter extends AbstractEnumConverter<AuditLogCategoryEnum> {

    public AuditLotCategoryConverter() {
      super(AuditLogCategoryEnum.class);
    }
  }
}
