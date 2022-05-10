package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogMhifRptViewService {

  List<ImportFileLogMhifRptView> search(ImportFileLogMhifRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(ImportFileLogMhifRptViewSearchCriteria criteria);
}
