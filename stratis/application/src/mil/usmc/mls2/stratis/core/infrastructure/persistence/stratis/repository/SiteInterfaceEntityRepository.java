package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInterfaceEntity;

import java.util.Optional;

public interface SiteInterfaceEntityRepository extends EntityRepository<SiteInterfaceEntity, Integer>, SiteInterfaceEntityRepositoryCustom {

  Optional<SiteInterfaceEntity> getByInterfaceName(String interfaceName);
}