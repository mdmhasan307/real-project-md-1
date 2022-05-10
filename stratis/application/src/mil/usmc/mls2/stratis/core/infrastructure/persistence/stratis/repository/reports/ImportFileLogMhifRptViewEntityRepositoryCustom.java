package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogMhifRptViewEntity;

import java.util.List;

public interface ImportFileLogMhifRptViewEntityRepositoryCustom {

  List<ImportFileLogMhifRptViewEntity> search(ImportFileLogMhifRptViewSearchCriteria criteria);
}
