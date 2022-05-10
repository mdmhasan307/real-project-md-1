package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Location;
import mil.usmc.mls2.stratis.core.domain.model.LocationSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<Location> findById(Integer id) {
    return locationRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Long count(LocationSearchCriteria criteria) {
    return locationRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Location> search(LocationSearchCriteria criteria) { return locationRepository.search(criteria); }

  @Override
  @Transactional
  public void update(Location location) { locationRepository.save(location); }
}
