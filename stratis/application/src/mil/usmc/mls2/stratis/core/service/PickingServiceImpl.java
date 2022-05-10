package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.core.domain.model.PickingSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.domain.repository.PickingRepository;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class PickingServiceImpl implements PickingService {

  private final PickingRepository pickingRepository;
  private final GlobalConstants globalConstants;
  private final LocationSortBuilder sortBuilder;

  @Override
  @Transactional(readOnly = true)
  public Optional<Picking> findById(Integer id) {
    return pickingRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(PickingSearchCriteria criteria) {
    return pickingRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Picking> search(PickingSearchCriteria criteria) {
    return pickingRepository.search(criteria);
  }

  @Override
  @Transactional
  public List<Picking> getPickingListForProcessing(PickingSearchCriteria criteria, Wac wac) {
    String mechFlag = wac.getMechanizedFlag();
    Integer tasksPerTrip = wac.getTasksPerTrip();
    if (tasksPerTrip == null || tasksPerTrip == 0) {
      tasksPerTrip = globalConstants.getTasksPerTrip();
    }

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

  @Transactional
  public void deAssignAllPicksForUser(Integer userId) {
    PickingSearchCriteria pickingSearchCriteria = new PickingSearchCriteria();
    pickingSearchCriteria.setAssignedUserId(userId);

    val picks = search(pickingSearchCriteria);

    picks.forEach(x -> {
      x.assignPick(null);
      pickingRepository.save(x);
    });
  }

  @Override
  @Transactional
  public void save(Picking pick) {
    pickingRepository.save(pick);
  }

  @Override
  @Transactional
  public void delete(Picking pick) {
    pickingRepository.delete(pick);
  }

  @Override
  @Transactional
  public void deleteByScn(String scn) {
    pickingRepository.deleteByScn(scn);
  }

  @Override
  @Transactional
  public void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate) {
    pickingRepository.deleteHistoryByScnAndDate(scn, createdDate);
  }

  @Override
  @Transactional
  public Optional<Long> findCountOfPickingHistory(String scn, String status, OffsetDateTime createdDate) {
    return pickingRepository.findCountOfPickingHistory(scn, status, createdDate);
  }

  private List<Picking> getMechFlagVPicks(PickingSearchCriteria criteria) {
    List<String> sides = new ArrayList<>();
    sides.add("A");
    criteria.setSides(sides);
    criteria.setSort("niinLocation.location.locLevel, niinLocation.location.bay, niinLocation.location.slot", SortOrder.ASC); //note these are location fields picking.niinLocation.location....
    return getPicksForProcessing(criteria);
  }

  /**
   * while the database could grab both side A and B at the same time the front end is expecting the order to be an alternating list of side A and B records.
   */
  private List<Picking> getMechFlagHPicks(PickingSearchCriteria criteria) {
    List<String> sides = new ArrayList<>();
    sides.add("A");
    criteria.setSides(sides);
    criteria.setSort("niinLocation.location.side, niinLocation.location.bay, niinLocation.location.locLevel, niinLocation.location.slot", SortOrder.ASC); //note these are location fields picking.niinLocation.location....

    val sideAResults = getPicksForProcessing(criteria);

    sides = new ArrayList<>();
    sides.add("B");
    criteria.setSides(sides);

    val sideBResults = getPicksForProcessing(criteria);

    return SMVUtility.mergeLists(sideAResults, sideBResults);
  }

  private List<Picking> getMechFlagNPicks(PickingSearchCriteria criteria, Integer tasksPerTrip) {
    criteria.setSort(sortBuilder.getSortString(LocationSortBuilder.QueryType.HIBERNATE_NIIN_LOCATION), SortOrder.ASC);

    criteria.setMaxItems(tasksPerTrip);
    val pickings = getPicksForProcessing(criteria);

    //assign the current logged in user to the picks found.
    pickings.forEach(x -> {
      x.assignPick(criteria.getAssignedUserId());
      pickingRepository.save(x);
    });

    return pickings;
  }

  private List<Picking> getPicksForProcessing(PickingSearchCriteria criteria) {
    return pickingRepository.getPicksForProcessing(criteria);
  }
}
