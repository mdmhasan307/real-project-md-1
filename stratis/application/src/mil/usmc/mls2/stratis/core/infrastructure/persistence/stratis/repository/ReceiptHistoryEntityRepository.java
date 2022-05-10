package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptHistoryEntity;

public interface ReceiptHistoryEntityRepository extends EntityRepository<ReceiptHistoryEntity, Integer>, ReceiptHistoryEntityRepositoryCustom {
}
