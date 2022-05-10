package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Location;
import mil.usmc.mls2.stratis.core.domain.model.LocationSearchCriteria;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {

  void save(Location location);

  Optional<Location> findById(Integer id);

  Long count(LocationSearchCriteria criteria);

  List<Location> search(LocationSearchCriteria criteria);
}
