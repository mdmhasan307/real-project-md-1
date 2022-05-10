package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptViewSearchCriteria;

import java.util.List;

public interface ImportFileLogGabfRptViewService {

  List<ImportFileLogGabfRptView> search(ImportFileLogGabfRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(ImportFileLogGabfRptViewSearchCriteria criteria);
}
