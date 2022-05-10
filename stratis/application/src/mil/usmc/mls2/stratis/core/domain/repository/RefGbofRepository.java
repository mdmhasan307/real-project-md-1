package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefGbof;
import mil.usmc.mls2.stratis.core.domain.model.RefGbofSearchCriteria;

import java.util.Set;

public interface RefGbofRepository {

  Set<RefGbof> search(RefGbofSearchCriteria criteria);

  void save(RefGbof refGbof);

  void deleteAll();
}
