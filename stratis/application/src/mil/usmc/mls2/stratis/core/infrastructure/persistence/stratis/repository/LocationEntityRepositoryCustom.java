package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.LocationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.LocationEntity;

import java.util.List;

public interface LocationEntityRepositoryCustom {

  long count(LocationSearchCriteria criteria);

  List<LocationEntity> search(LocationSearchCriteria criteria);

}
