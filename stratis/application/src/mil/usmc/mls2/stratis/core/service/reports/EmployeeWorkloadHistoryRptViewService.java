package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.StdReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptViewSearchCriteria;

import java.util.List;

public interface EmployeeWorkloadHistoryRptViewService {

  List<EmployeeWorkloadHistoryRptView> search(EmployeeWorkloadHistoryRptViewSearchCriteria criteria);

  List<StdReportColumnBean> searchForAdfReport(EmployeeWorkloadHistoryRptViewSearchCriteria criteria);
}
