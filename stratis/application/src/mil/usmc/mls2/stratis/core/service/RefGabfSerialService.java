package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerial;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerialSearchCriteria;

import java.util.Set;

public interface RefGabfSerialService {

  void save(RefGabfSerial refGabfSerial);

  Set<RefGabfSerial> search(RefGabfSerialSearchCriteria criteria);

  void deleteAll();
}
