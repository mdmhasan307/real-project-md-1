package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogGbofRptViewEntity;

import java.util.List;

public interface ImportFileLogGbofRptViewEntityRepositoryCustom {

  List<ImportFileLogGbofRptViewEntity> search(ImportFileLogGbofRptViewSearchCriteria criteria);
}
