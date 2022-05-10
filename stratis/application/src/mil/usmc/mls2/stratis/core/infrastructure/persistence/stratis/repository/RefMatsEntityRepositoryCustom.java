package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefMatsSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefMatsEntity;

import java.util.Set;

public interface RefMatsEntityRepositoryCustom {

  Set<RefMatsEntity> search(RefMatsSearchCriteria criteria);

  Set<String> findMissingNiins();
}
