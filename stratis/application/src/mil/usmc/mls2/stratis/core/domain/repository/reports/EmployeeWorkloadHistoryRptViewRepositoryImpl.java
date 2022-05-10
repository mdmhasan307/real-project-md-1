package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.EmployeeWorkloadHistoryRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.EmployeeWorkloadHistoryRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class EmployeeWorkloadHistoryRptViewRepositoryImpl implements EmployeeWorkloadHistoryRptViewRepository {

  private static final EmployeeWorkloadHistoryRptViewMapper EMPLOYEE_WORKLOAD_HISTORY_RPT_VIEW_MAPPER = EmployeeWorkloadHistoryRptViewMapper.INSTANCE;

  private final EmployeeWorkloadHistoryRptViewEntityRepository employeeWorkloadHistoryRptViewEntityRepository;

  @Override
  public List<EmployeeWorkloadHistoryRptView> search(EmployeeWorkloadHistoryRptViewSearchCriteria criteria) {
    val results = employeeWorkloadHistoryRptViewEntityRepository.search(criteria);
    return EMPLOYEE_WORKLOAD_HISTORY_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
