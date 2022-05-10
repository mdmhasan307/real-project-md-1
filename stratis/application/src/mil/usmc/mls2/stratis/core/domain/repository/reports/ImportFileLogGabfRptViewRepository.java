package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogGabfRptViewRepository {

  List<ImportFileLogGabfRptView> search(ImportFileLogGabfRptViewSearchCriteria criteria);
}
