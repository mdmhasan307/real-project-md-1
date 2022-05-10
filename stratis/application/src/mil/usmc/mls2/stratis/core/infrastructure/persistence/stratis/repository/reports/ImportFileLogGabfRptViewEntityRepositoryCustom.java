package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ImportFileLogGabfRptViewEntity;

import java.util.List;

public interface ImportFileLogGabfRptViewEntityRepositoryCustom {

  List<ImportFileLogGabfRptViewEntity> search(ImportFileLogGabfRptViewSearchCriteria criteria);
}
