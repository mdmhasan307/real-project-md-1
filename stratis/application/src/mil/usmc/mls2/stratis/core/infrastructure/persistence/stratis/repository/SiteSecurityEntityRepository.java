package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteSecurityCompositeId;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteSecurityEntity;

public interface SiteSecurityEntityRepository extends EntityRepository<SiteSecurityEntity, SiteSecurityCompositeId>, SiteSecurityEntityRepositoryCustom {
}
