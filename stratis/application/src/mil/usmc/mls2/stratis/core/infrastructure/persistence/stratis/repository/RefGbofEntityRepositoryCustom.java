package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefGbofSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGbofEntity;

import java.util.Set;

public interface RefGbofEntityRepositoryCustom {

  Set<RefGbofEntity> search(RefGbofSearchCriteria criteria);
}
