package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNum;
import mil.usmc.mls2.stratis.core.domain.model.InvSerialLotNumSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface InvSerialLotNumService {

  Optional<InvSerialLotNum> findById(Integer id);

  Long count(InvSerialLotNumSearchCriteria criteria);

  List<InvSerialLotNum> search(InvSerialLotNumSearchCriteria criteria);

  void save(InvSerialLotNum serial);

  void delete(InvSerialLotNum serial);
}
