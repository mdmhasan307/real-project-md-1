package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptViewSearchCriteria;

import java.util.List;

public interface InventoryHistoryRptViewRepository {

  List<InventoryHistoryRptView> search(InventoryHistoryRptViewSearchCriteria criteria);
}
