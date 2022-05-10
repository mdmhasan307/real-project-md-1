package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.Mls2SitesEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.EntityRepository;

import java.util.Optional;
import java.util.UUID;

public interface Mls2SitesEntityRepository extends EntityRepository<Mls2SitesEntity, UUID>, Mls2SitesEntityRepositoryCustom {

  Optional<Mls2SitesEntity> getBySiteName(String siteName);
}
