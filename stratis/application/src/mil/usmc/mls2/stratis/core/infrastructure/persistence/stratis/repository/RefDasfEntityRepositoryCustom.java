package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefDasfSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDasfEntity;

import java.util.Set;

public interface RefDasfEntityRepositoryCustom {

  Set<RefDasfEntity> search(RefDasfSearchCriteria criteria);

  Set<String> findMissingNiins();
}
