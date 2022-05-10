package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogMatsRptViewService {

  List<ImportFileLogMatsRptView> search(ImportFileLogMatsRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(ImportFileLogMatsRptViewSearchCriteria criteria);
}
