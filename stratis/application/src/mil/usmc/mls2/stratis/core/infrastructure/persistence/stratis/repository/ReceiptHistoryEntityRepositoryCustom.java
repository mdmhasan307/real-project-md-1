package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistorySearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptHistoryEntity;

import java.util.List;

public interface ReceiptHistoryEntityRepositoryCustom {

  List<ReceiptHistoryEntity> search(ReceiptHistorySearchCriteria criteria);

  Long count(ReceiptHistorySearchCriteria criteria);
}
