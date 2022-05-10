package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InventoryHistoryRptViewEntity;

import java.util.List;

public interface InventoryHistoryRptViewEntityRepositoryCustom {

  List<InventoryHistoryRptViewEntity> search(InventoryHistoryRptViewSearchCriteria criteria);
}
