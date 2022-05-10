package mil.usmc.mls2.stratis.core.infrastructure.mapper.reports;

import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptView;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InventoryHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface InventoryHistoryRptViewMapper {

  InventoryHistoryRptViewMapper INSTANCE = Mappers.getMapper(InventoryHistoryRptViewMapper.class);

  InventoryHistoryRptView entityToModel(InventoryHistoryRptViewEntity entity);

  List<InventoryHistoryRptView> entityListToModelList(List<InventoryHistoryRptViewEntity> entitySet);

  List<ReportColumnBean> modelListToReportColumnList(List<InventoryHistoryRptView> modelSet);

  @Mapping(source = "locationLabel", target = "location")
  @Mapping(source = "completedBy", target = "employee")
  @Mapping(source = "totalValue", target = "priceTotal")
  @Mapping(source = "completedDate", target = "completedDate", qualifiedByName = "dateFormatting")
  ReportColumnBean modelToReportColumn(InventoryHistoryRptView importFile);

  @Named("dateFormatting")
  default String convertDate(OffsetDateTime date) {
    if (date == null) return null;
    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN);
    return date.format(formatter);
  }
}
