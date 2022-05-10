package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.NiinLocation;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocationSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;

import java.util.List;
import java.util.Optional;

public interface NiinLocationService {

  Optional<NiinLocation> findById(Integer id);

  Long count(NiinLocationSearchCriteria criteria);

  List<NiinLocation> search(NiinLocationSearchCriteria criteria);

  void save(NiinLocation location);

  List<NiinLocation> getNiinLocationListForProcessing(NiinLocationSearchCriteria criteria, Wac wac);
}
