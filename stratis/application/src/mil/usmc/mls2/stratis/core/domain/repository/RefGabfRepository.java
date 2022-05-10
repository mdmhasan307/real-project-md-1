package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefGabf;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSearchCriteria;

import java.util.Set;

public interface RefGabfRepository {

  void callPopulateGcssReconHist();

  Set<RefGabf> search(RefGabfSearchCriteria criteria);

  void save(RefGabf refGabf);

  void truncate();

  void deleteAll();
}
