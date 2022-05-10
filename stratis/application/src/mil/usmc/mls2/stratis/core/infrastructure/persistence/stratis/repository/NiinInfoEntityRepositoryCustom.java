package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinInfoEntity;

import java.util.Set;

public interface NiinInfoEntityRepositoryCustom {

  long count(NiinInfoSearchCriteria criteria);

  Set<NiinInfoEntity> search(NiinInfoSearchCriteria criteria);
}
