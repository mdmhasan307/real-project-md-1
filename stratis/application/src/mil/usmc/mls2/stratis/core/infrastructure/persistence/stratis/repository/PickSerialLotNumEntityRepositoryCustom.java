package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickSerialLotNumEntity;

import java.util.List;

public interface PickSerialLotNumEntityRepositoryCustom {

  List<PickSerialLotNumEntity> search(PickSerialLotNumSearchCriteria criteria);
}
