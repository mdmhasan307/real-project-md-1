package mil.usmc.mls2.stratis.core.infrastructure.mapper.reports;

import lombok.val;
import mil.stratis.view.reports.StdReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptView;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.EmployeeWorkloadHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface EmployeeWorkloadHistoryRptViewMapper {

  EmployeeWorkloadHistoryRptViewMapper INSTANCE = Mappers.getMapper(EmployeeWorkloadHistoryRptViewMapper.class);

  @Mapping(source = "id.user", target = "user")
  @Mapping(source = "id.transDate", target = "transDate")
  EmployeeWorkloadHistoryRptView entityToModel(EmployeeWorkloadHistoryRptViewEntity entity);

  List<EmployeeWorkloadHistoryRptView> entityListToModelList(List<EmployeeWorkloadHistoryRptViewEntity> entitySet);

  List<StdReportColumnBean> modelListToReportColumnList(List<EmployeeWorkloadHistoryRptView> modelSet);

  @Mapping(source = "receiptCount", target = "receipts")
  @Mapping(source = "stowCount", target = "stows")
  @Mapping(source = "pickCount", target = "picks")
  @Mapping(source = "packConsolidationCount", target = "packs")
  @Mapping(source = "inventoryItemCount", target = "invs")
  @Mapping(source = "otherCount", target = "totals")
  @Mapping(source = "user", target = "employee")
  @Mapping(source = "inventoryDollarValue", target = "invsDollarValue")
  @Mapping(source = "stowDollarValue", target = "stowsDollarValue")
  @Mapping(source = "pickDollarValue", target = "picksDollarValue")
  @Mapping(source = "packDollarValue", target = "packsDollarValue")
  @Mapping(source = "transDate", target = "transDate", qualifiedByName = "dateFormatting")
  StdReportColumnBean modelToReportColumn(EmployeeWorkloadHistoryRptView importFile);

  @Named("dateFormatting")
  default String convertDate(OffsetDateTime date) {
    if (date == null) return null;
    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_FORMATTER_PATTERN);
    return date.format(formatter);
  }
}
