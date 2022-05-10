package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefMats;
import mil.usmc.mls2.stratis.core.domain.model.RefMatsSearchCriteria;

import java.util.Set;

public interface RefMatsRepository {

  Set<RefMats> search(RefMatsSearchCriteria criteria);

  void save(RefMats refMats);

  void deleteAll();

  void truncate();

  Set<RefMats> findAll();

  Set<String> findMissingNiins();
}
