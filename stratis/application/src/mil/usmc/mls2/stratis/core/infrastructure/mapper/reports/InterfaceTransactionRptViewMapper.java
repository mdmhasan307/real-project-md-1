package mil.usmc.mls2.stratis.core.infrastructure.mapper.reports;

import lombok.val;
import mil.stratis.view.reports.StdReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptView;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InterfaceTransactionRptViewEntity;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface InterfaceTransactionRptViewMapper {

  InterfaceTransactionRptViewMapper INSTANCE = Mappers.getMapper(InterfaceTransactionRptViewMapper.class);

  InterfaceTransactionRptView entityToModel(InterfaceTransactionRptViewEntity entity);

  List<InterfaceTransactionRptView> entityListToModelList(List<InterfaceTransactionRptViewEntity> entitySet);

  List<StdReportColumnBean> modelListToReportColumnList(List<InterfaceTransactionRptView> modelSet);

  @Mapping(source = "date", target = "transDate", qualifiedByName = "dateFormatting")
  @Mapping(source = "timestamp", target = "outputDate", qualifiedByName = "timestampFormatting")
  @Mapping(source = "transactionType", target = "transType")
  StdReportColumnBean modelToReportColumn(InterfaceTransactionRptView importFile);

  @Named("dateFormatting")
  default String convertDate(OffsetDateTime date) {
    if (date == null) return null;
    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN);
    return date.format(formatter);
  }

  @Named("timestampFormatting")
  default String convertTimestampToOutputDate(String timestamp) {
    val dateService = ContextUtils.getBean(DateService.class);
    return dateService.formatForAdfDisplay(timestamp, DateConstants.ADF_REPORT_DATE_TIME_WITH_JULIAN, DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN, true, false);
  }
}
