package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerial;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerialSearchCriteria;

import java.util.Set;

public interface RefGabfSerialRepository {

  Set<RefGabfSerial> search(RefGabfSerialSearchCriteria criteria);

  void save(RefGabfSerial refDasfSerial);

  void deleteAll();
}
