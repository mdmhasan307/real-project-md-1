package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptViewSearchCriteria;

import java.util.List;

public interface ReceiptHistRptViewRepository {

  List<ReceiptHistRptView> search(ReceiptHistRptViewSearchCriteria criteria);
}
