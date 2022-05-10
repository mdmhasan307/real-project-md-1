package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogGbofRptViewRepository {

  List<ImportFileLogGbofRptView> search(ImportFileLogGbofRptViewSearchCriteria criteria);
}
