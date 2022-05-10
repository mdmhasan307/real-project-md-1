package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerialSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGabfSerialEntity;

import java.util.Set;

public interface RefGabfSerialEntityRepositoryCustom {

  Set<RefGabfSerialEntity> search(RefGabfSerialSearchCriteria criteria);
}
