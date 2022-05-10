package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNumSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InvSerialLotNumEntity;

import java.util.List;

public interface InvSerialLotNumEntityRepositoryCustom {

  Long count(InvSerialLotNumSearchCriteria criteria);

  List<InvSerialLotNumEntity> search(InvSerialLotNumSearchCriteria criteria);
}
