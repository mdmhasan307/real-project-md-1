package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogDasfRptViewEntity;

import java.util.List;

public interface ImportFileLogDasfRptViewEntityRepositoryCustom {

  List<ImportFileLogDasfRptViewEntity> search(ImportFileLogDasfRptViewSearchCriteria criteria);
}
