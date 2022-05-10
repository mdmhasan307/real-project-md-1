package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogMhifRptViewRepository {

  List<ImportFileLogMhifRptView> search(ImportFileLogMhifRptViewSearchCriteria criteria);
}
