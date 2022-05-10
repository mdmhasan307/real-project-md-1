package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefGabfSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGabfEntity;

import java.util.Set;

public interface RefGabfEntityRepositoryCustom {

  void callPopulateGcssReconHist();
  
  Set<RefGabfEntity> search(RefGabfSearchCriteria criteria);
}
