package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogDasfRptViewService {

  List<ImportFileLogDasfRptView> search(ImportFileLogDasfRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(ImportFileLogDasfRptViewSearchCriteria criteria);
}
