package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;

import java.util.Optional;
import java.util.Set;

public interface NiinInfoRepository {

  void save(NiinInfo location);

  Optional<NiinInfo> findById(Integer id);

  Optional<NiinInfo> findByNiin(String niin);

  Long count(NiinInfoSearchCriteria criteria);

  Set<NiinInfo> search(NiinInfoSearchCriteria criteria);
}
