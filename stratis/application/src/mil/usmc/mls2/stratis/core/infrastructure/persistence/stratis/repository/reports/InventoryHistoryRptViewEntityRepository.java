package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.InventoryHistoryRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.UUID;

public interface InventoryHistoryRptViewEntityRepository extends EntityRepository<InventoryHistoryRptViewEntity, UUID>, InventoryHistoryRptViewEntityRepositoryCustom {

}
