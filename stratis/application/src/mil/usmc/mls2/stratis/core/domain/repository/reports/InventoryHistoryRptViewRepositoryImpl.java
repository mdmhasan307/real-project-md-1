package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.InventoryHistoryRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.InventoryHistoryRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class InventoryHistoryRptViewRepositoryImpl implements InventoryHistoryRptViewRepository {

  private static final InventoryHistoryRptViewMapper INVENTORY_HISTORY_RPT_VIEW_MAPPER = InventoryHistoryRptViewMapper.INSTANCE;

  private final InventoryHistoryRptViewEntityRepository InventoryHistoryRptViewEntityRepository;

  @Override
  public List<InventoryHistoryRptView> search(InventoryHistoryRptViewSearchCriteria criteria) {
    val results = InventoryHistoryRptViewEntityRepository.search(criteria);
    return INVENTORY_HISTORY_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
