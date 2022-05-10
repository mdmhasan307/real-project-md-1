package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogDasfRptViewRepository {

  List<ImportFileLogDasfRptView> search(ImportFileLogDasfRptViewSearchCriteria criteria);
}
