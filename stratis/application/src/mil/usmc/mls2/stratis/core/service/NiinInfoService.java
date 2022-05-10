package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;

import java.util.Optional;
import java.util.Set;

public interface NiinInfoService {

  Optional<NiinInfo> findById(Integer id);

  Optional<NiinInfo> findByNiin(String niin);

  void save(NiinInfo location);

  Set<NiinInfo> search(NiinInfoSearchCriteria criteria);
}
