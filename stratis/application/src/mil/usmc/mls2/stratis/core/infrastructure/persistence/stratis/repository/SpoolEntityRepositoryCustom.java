package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.SpoolSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SpoolEntity;

import java.util.Optional;
import java.util.Set;

public interface SpoolEntityRepositoryCustom {

  Optional<SpoolEntity> searchSingle(SpoolSearchCriteria criteria);

  Set<SpoolEntity> search(SpoolSearchCriteria criteria);

  Long count(SpoolSearchCriteria criteria);
}
