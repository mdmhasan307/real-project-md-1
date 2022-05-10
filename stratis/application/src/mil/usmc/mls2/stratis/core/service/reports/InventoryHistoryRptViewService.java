package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptViewSearchCriteria;

import java.util.List;

public interface InventoryHistoryRptViewService {

  List<InventoryHistoryRptView> search(InventoryHistoryRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(InventoryHistoryRptViewSearchCriteria criteria);
}
