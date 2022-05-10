package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefMhif;
import mil.usmc.mls2.stratis.core.domain.model.RefMhifSearchCriteria;

import java.util.Set;

public interface RefMhifService {

  void save(RefMhif refMhif);

  Set<RefMhif> search(RefMhifSearchCriteria criteria);

  void deleteAll();

  void callProcessMhif();
}
