package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.StdReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptViewSearchCriteria;

import java.util.List;

public interface InterfaceTransactionRptViewService {

  List<InterfaceTransactionRptView> search(InterfaceTransactionRptViewSearchCriteria criteria);

  List<StdReportColumnBean> searchForAdfReport(InterfaceTransactionRptViewSearchCriteria criteria);
}
