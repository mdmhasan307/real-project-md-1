package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.PickSerialLotNumSearchCriteria;

import java.util.List;

public interface PickSerialLotNumRepository {

  List<PickSerialLotNum> search(PickSerialLotNumSearchCriteria criteria);

  void delete(PickSerialLotNum pickSerialLotNum);

  void save(PickSerialLotNum pickSerialLotNum);
}
