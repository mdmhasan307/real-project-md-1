package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefMhif;
import mil.usmc.mls2.stratis.core.domain.model.RefMhifSearchCriteria;

import java.util.Set;

public interface RefMhifRepository {

  Set<RefMhif> search(RefMhifSearchCriteria criteria);

  void save(RefMhif refMhif);

  void callProcessMhif();

  void deleteAll();
}
