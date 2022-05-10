package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.RefGabf;
import mil.usmc.mls2.stratis.core.domain.model.RefGabfSearchCriteria;

import java.util.Set;

public interface RefGabfService {

  void callPopulateGcssReconHist();
  
  void save(RefGabf refGabf);

  Set<RefGabf> search(RefGabfSearchCriteria criteria);

  void deleteAll();
}
