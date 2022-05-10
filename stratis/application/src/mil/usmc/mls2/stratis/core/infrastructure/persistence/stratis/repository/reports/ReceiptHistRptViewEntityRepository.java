package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports.ReceiptHistRptViewEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

public interface ReceiptHistRptViewEntityRepository extends EntityRepository<ReceiptHistRptViewEntity, Integer>, ReceiptHistRptViewEntityRepositoryCustom {

}
