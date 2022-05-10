package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.StowSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.StowEntity;

import java.util.List;

public interface StowEntityRepositoryCustom {

  long count(StowSearchCriteria criteria);

  List<StowEntity> search(StowSearchCriteria criteria);
}
