package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.InterfaceTransactionRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.InterfaceTransactionRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class InterfaceTransactionRptViewRepositoryImpl implements InterfaceTransactionRptViewRepository {

  private static final InterfaceTransactionRptViewMapper SUMOUT_SPOOL_DETAIL_GCSS_VIEW_MAPPER = InterfaceTransactionRptViewMapper.INSTANCE;

  private final InterfaceTransactionRptViewEntityRepository sumoutSpoolDetailGcssRptViewEntityRepository;

  @Override
  public List<InterfaceTransactionRptView> search(InterfaceTransactionRptViewSearchCriteria criteria) {
    val results = sumoutSpoolDetailGcssRptViewEntityRepository.search(criteria);
    return SUMOUT_SPOOL_DETAIL_GCSS_VIEW_MAPPER.entityListToModelList(results);
  }
}
