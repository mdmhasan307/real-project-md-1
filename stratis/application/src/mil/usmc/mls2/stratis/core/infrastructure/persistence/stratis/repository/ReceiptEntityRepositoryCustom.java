package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.ReceiptSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptEntity;

import java.util.List;

public interface ReceiptEntityRepositoryCustom {

  List<ReceiptEntity> search(ReceiptSearchCriteria criteria);

  Long count(ReceiptSearchCriteria criteria);
}
