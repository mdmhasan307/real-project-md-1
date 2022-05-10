package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.PickingSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickingEntity;

import java.util.List;

public interface PickingEntityRepositoryCustom {

  Long count(PickingSearchCriteria criteria);

  List<PickingEntity> search(PickingSearchCriteria criteria);

  List<PickingEntity> getPicksForProcessing(PickingSearchCriteria criteria);
}
