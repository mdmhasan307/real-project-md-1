package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefGbof;
import mil.usmc.mls2.stratis.core.domain.model.RefGbofSearchCriteria;

import java.util.Set;

public interface RefGbofService {

  void save(RefGbof refGbof);

  Set<RefGbof> search(RefGbofSearchCriteria criteria);

  void deleteAll();
}
