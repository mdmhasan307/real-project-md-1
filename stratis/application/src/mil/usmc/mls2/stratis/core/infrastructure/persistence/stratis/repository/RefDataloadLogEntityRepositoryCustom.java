package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLogSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDataloadLogEntity;

import java.util.Set;

public interface RefDataloadLogEntityRepositoryCustom {

  Set<RefDataloadLogEntity> search(RefDataloadLogSearchCriteria criteria);
}
