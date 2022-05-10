package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SiteEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.UUID;

public interface SiteEntityRepository extends EntityRepository<SiteEntity, UUID>, SiteEntityRepositoryCustom {}
