package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefMats;
import mil.usmc.mls2.stratis.core.domain.model.RefMatsSearchCriteria;

import java.util.Set;

public interface RefMatsService {

  void save(RefMats RefMats);

  Set<RefMats> search(RefMatsSearchCriteria criteria);

  void deleteAll();

  Set<RefMats> findAll();
}
