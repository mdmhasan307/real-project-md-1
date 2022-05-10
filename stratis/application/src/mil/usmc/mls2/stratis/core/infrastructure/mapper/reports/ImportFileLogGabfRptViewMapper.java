package mil.usmc.mls2.stratis.core.infrastructure.mapper.reports;

import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptView;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogGabfRptViewEntity;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface ImportFileLogGabfRptViewMapper {

  ImportFileLogGabfRptViewMapper INSTANCE = Mappers.getMapper(ImportFileLogGabfRptViewMapper.class);

  ImportFileLogGabfRptView entityToModel(ImportFileLogGabfRptViewEntity importFileLogGabfRptViewEntity);

  List<ImportFileLogGabfRptView> entityListToModelList(List<ImportFileLogGabfRptViewEntity> entityList);

  List<ReportColumnBean> modelListToReportColumnList(List<ImportFileLogGabfRptView> modelList);

  @Mapping(source = "createdDate", target = "createdDate", qualifiedByName = "dateFormatting")
  @Mapping(source = "status", target = "dataRow")
  ReportColumnBean modelToReportColumn(ImportFileLogGabfRptView importFile);

  @Named("dateFormatting")
  default String convertDate(OffsetDateTime date) {
    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN);
    return date.format(formatter);
  }
}
