package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogGbofRptViewService {

  List<ImportFileLogGbofRptView> search(ImportFileLogGbofRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(ImportFileLogGbofRptViewSearchCriteria criteria);
}
