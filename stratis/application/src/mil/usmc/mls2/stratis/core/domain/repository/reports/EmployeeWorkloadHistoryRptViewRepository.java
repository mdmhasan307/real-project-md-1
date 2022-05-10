package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptViewSearchCriteria;

import java.util.List;

public interface EmployeeWorkloadHistoryRptViewRepository {

  List<EmployeeWorkloadHistoryRptView> search(EmployeeWorkloadHistoryRptViewSearchCriteria criteria);
}
