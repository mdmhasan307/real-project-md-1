package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ReceiptHistRptViewEntity;

import java.util.List;

public interface ReceiptHistRptViewEntityRepositoryCustom {

  List<ReceiptHistRptViewEntity> search(ReceiptHistRptViewSearchCriteria criteria);
}
