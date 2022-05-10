package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogMatsRptViewRepository {

  List<ImportFileLogMatsRptView> search(ImportFileLogMatsRptViewSearchCriteria criteria);
}
