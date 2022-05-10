package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefMhifSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefMhifEntity;

import java.util.Set;

public interface RefMhifEntityRepositoryCustom {

  Set<RefMhifEntity> search(RefMhifSearchCriteria criteria);

  void callProcessMhif();
}
