package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.SerialLotNumTrackSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SerialLotNumTrackEntity;

import java.util.List;

public interface SerialLotNumTrackEntityRepositoryCustom {
  List<SerialLotNumTrackEntity> search(SerialLotNumTrackSearchCriteria criteria);
}
