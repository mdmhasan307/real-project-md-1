package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNumSearchCriteria;

import java.util.List;

public interface PickSerialLotNumService {

  List<PickSerialLotNum> search(PickSerialLotNumSearchCriteria criteria);

  void delete(PickSerialLotNum serial);

  void save(PickSerialLotNum serial);
}
