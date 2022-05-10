package mil.usmc.mls2.stratis.core.service.reports;

import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptViewSearchCriteria;

import java.util.List;

public interface ReceiptHistRptViewService {

  List<ReceiptHistRptView> search(ReceiptHistRptViewSearchCriteria criteria);

  List<ReportColumnBean> searchForAdfReport(ReceiptHistRptViewSearchCriteria criteria);
}
