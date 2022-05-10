package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InterfaceTransactionRptViewEntity;

import java.util.List;

public interface InterfaceTransactionRptViewEntityRepositoryCustom {

  List<InterfaceTransactionRptViewEntity> search(InterfaceTransactionRptViewSearchCriteria criteria);
}
