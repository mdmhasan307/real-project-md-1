package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefDasf;
import mil.usmc.mls2.stratis.core.domain.model.RefDasfSearchCriteria;

import java.util.Set;

public interface RefDasfService {

  void save(RefDasf refDasf);

  Set<RefDasf> search(RefDasfSearchCriteria criteria);

  void deleteAll();
}
