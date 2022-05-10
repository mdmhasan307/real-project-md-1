package mil.usmc.mls2.stratis.core.infrastructure.mapper.reports;

import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptView;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogMatsRptViewEntity;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface ImportFileLogMatsRptViewMapper {

  ImportFileLogMatsRptViewMapper INSTANCE = Mappers.getMapper(ImportFileLogMatsRptViewMapper.class);

  ImportFileLogMatsRptView entityToModel(ImportFileLogMatsRptViewEntity importFileLogMatsRptViewEntity);

  List<ImportFileLogMatsRptView> entityListToModelList(List<ImportFileLogMatsRptViewEntity> entityList);

  List<ReportColumnBean> modelListToReportColumnList(List<ImportFileLogMatsRptView> modelList);

  @Mapping(source = "createdDate", target = "createdDate", qualifiedByName = "dateFormatting")
  @Mapping(source = "status", target = "dataRow")
  ReportColumnBean modelToReportColumn(ImportFileLogMatsRptView importFile);

  @Named("dateFormatting")
  default String convertDate(OffsetDateTime date) {
    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN);
    return date.format(formatter);
  }
}
