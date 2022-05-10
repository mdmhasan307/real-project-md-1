package mil.usmc.mls2.stratis.core.domain.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptViewSearchCriteria;

import java.util.List;

public interface InterfaceTransactionRptViewRepository {

  List<InterfaceTransactionRptView> search(InterfaceTransactionRptViewSearchCriteria criteria);
}
