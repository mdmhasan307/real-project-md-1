package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogMatsRptViewEntity;

import java.util.List;

public interface ImportFileLogMatsRptViewEntityRepositoryCustom {

  List<ImportFileLogMatsRptViewEntity> search(ImportFileLogMatsRptViewSearchCriteria criteria);
}
