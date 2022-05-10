package mil.usmc.mls2.stratis.core.infrastructure.mapper.reports;

import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptView;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ReceiptHistRptViewEntity;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface ReceiptHistRptViewMapper {

  ReceiptHistRptViewMapper INSTANCE = Mappers.getMapper(ReceiptHistRptViewMapper.class);

  @Mapping(source = "id.rcn", target = "rcn")
  @Mapping(source = "id.sid", target = "sid")
  ReceiptHistRptView entityToModel(ReceiptHistRptViewEntity entity);

  List<ReceiptHistRptView> entityListToModelList(List<ReceiptHistRptViewEntity> entitySet);

  List<ReportColumnBean> modelListToReportColumnList(List<ReceiptHistRptView> modelSet);

  @Mapping(source = "dateReceived", target = "dateReceived", qualifiedByName = "dateFormatting")
  @Mapping(source = "stowedDate", target = "dateStowed", qualifiedByName = "dateFormatting")
  @Mapping(source = "locationLabel", target = "location")
  ReportColumnBean modelToReportColumn(ReceiptHistRptView importFile);

  @Named("dateFormatting")
  default String convertDate(OffsetDateTime date) {
    if (date == null) return null;
    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN);
    return date.format(formatter);
  }
}
