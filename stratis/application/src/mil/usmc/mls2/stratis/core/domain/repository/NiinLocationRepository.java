package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.NiinLocation;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocationSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface NiinLocationRepository {
  void save(NiinLocation location);

  Optional<NiinLocation> findById(Integer id);

  long count(NiinLocationSearchCriteria criteria);

  List<NiinLocation> search(NiinLocationSearchCriteria criteria);
}
