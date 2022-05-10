package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.EmployeeWorkloadHistoryRptViewEntity;

import java.util.List;

public interface EmployeeWorkloadHistoryRptViewEntityRepositoryCustom {

  List<EmployeeWorkloadHistoryRptViewEntity> search(EmployeeWorkloadHistoryRptViewSearchCriteria criteria);
}
