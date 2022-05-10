package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocation;
import mil.usmc.mls2.stratis.core.domain.model.NiinLocationSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.domain.repository.NiinLocationRepository;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class NiinLocationServiceImpl implements NiinLocationService {

  private final NiinLocationRepository niinLocationRepository;
  private final LocationSortBuilder sortBuilder;

  @Override
  @Transactional(readOnly = true)
  public Optional<NiinLocation> findById(Integer id) {
    return niinLocationRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(NiinLocationSearchCriteria criteria) {
    return niinLocationRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<NiinLocation> search(NiinLocationSearchCriteria criteria) {
    return niinLocationRepository.search(criteria);
  }

  @Override
  @Transactional
  public void save(NiinLocation location) {
    niinLocationRepository.save(location);
  }

  @Override
  @Transactional
  public List<NiinLocation> getNiinLocationListForProcessing(NiinLocationSearchCriteria criteria, Wac wac) {
    String mechFlag = wac.getMechanizedFlag();
    Integer tasksPerTrip = wac.getTasksPerTrip();

    switch (mechFlag) {
      case "V":
        return getMechFlagVPicks(criteria);
      case "H":
        return getMechFlagHPicks(criteria);
      case "N":
        return getMechFlagNPicks(criteria, tasksPerTrip);
      default:
        throw new StratisRuntimeException(("Invalid Mech Flag identified"));
    }
  }

  private List<NiinLocation> getMechFlagVPicks(NiinLocationSearchCriteria criteria) {
    List<String> sides = new ArrayList<>();
    sides.add("A");
    criteria.setSides(sides);
    criteria.setSort("location.locLevel, location.bay, location.slot", SortOrder.ASC); //note these are location fields picking.niinLocation.location....
    return search(criteria);
  }

  private List<NiinLocation> getMechFlagHPicks(NiinLocationSearchCriteria criteria) {
    List<String> sides = new ArrayList<>();
    sides.add("A");
    sides.add("B");
    criteria.setSides(sides);
    criteria.setSort("location.side, location.bay, location.locLevel, location.slot", SortOrder.ASC); //note these are location fields picking.niinLocation.location....

    return search(criteria);
  }

  private List<NiinLocation> getMechFlagNPicks(NiinLocationSearchCriteria criteria, Integer tasksPerTrip) {
    criteria.setMaxItems(tasksPerTrip);
    criteria.setSort(sortBuilder.getSortString(LocationSortBuilder.QueryType.HIBERNATE_LOCATION), SortOrder.ASC); //note these are location fields picking.niinLocation.location and wac....
    return search(criteria);
  }
}
