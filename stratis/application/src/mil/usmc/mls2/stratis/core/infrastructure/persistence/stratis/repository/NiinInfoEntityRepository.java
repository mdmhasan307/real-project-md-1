package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinInfoEntity;

import java.util.Optional;

public interface NiinInfoEntityRepository extends EntityRepository<NiinInfoEntity, Integer>, NiinInfoEntityRepositoryCustom {

  Optional<NiinInfoEntity> findByNiin(String niin);
}
