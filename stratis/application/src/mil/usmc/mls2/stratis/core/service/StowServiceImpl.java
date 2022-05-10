package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.Stow;
import mil.usmc.mls2.stratis.core.domain.model.StowSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.domain.repository.StowRepository;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class StowServiceImpl implements StowService {

  private final StowRepository stowRepository;
  private final LocationSortBuilder sortBuilder;

  @Override
  @Transactional(readOnly = true)
  public Optional<Stow> findById(Integer id) {
    return stowRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(StowSearchCriteria criteria) {
    return stowRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Stow> search(StowSearchCriteria criteria) {
    return stowRepository.search(criteria);
  }

  /**
   * Unassigned stows need to be assigned to the next user that logs in.
   * Unless they are at a mechanized wac. This method merges both assigned
   * and unassigned lists of stows. It also handles the side effect of assigning
   * the new stows to the user.
   */
  @Override
  @Transactional
  public List<Stow> createAssignedListOfStowsForUser(StowSearchCriteria criteria, Integer uid, Wac wac, Long limit) {
    val isMechanized = !wac.getMechanizedFlag().equalsIgnoreCase("N");
    val entity = assignedStowsByMechFlag(criteria, wac.getMechanizedFlag());

    if (null != entity) {
      val assignedStows = entity.stream()
          .filter(Objects::nonNull)
          .distinct()
          .filter(s -> !isMechanized)
          .filter(s -> uid.equals(s.getAssignToUser()));

      val unassignedStows = entity.stream()
          .filter(Objects::nonNull)
          .distinct()
          .filter(s -> null == s.getAssignToUser())
          .peek(s -> {
            if (!isMechanized) {
              s.setAssignToUser(uid);
              update(s);
            }
          });

      // NOTE: SIDs displayed is arbitrarily limited to 12; However, it is possible for a user to scan
      // and assign more than 12 SIDs to themselves. If they have scanned more than 12 stows they will
      // be redirected to the home screen after they have processed their stows and the remaining stows
      // will be added to the StowingListManager (up to 12). This behavior mimics how the old handheld
      // code worked.
      return Stream.concat(assignedStows, unassignedStows)
          .limit(limit)
          .collect(Collectors.toList());
    }
    else return Collections.emptyList();
  }

  @Override
  @Transactional
  public void update(Stow stow) {
    stowRepository.save(stow);
  }

  @Override
  @Transactional
  public Integer updateStowScannedFlag(Wac wac, String sid, Integer uid, boolean isMobile) {

    val criteria = StowSearchCriteria.builder()
        .sid(sid)
        .statuses(Collections.singletonList("STOW READY"))
        .build();

    val stowResults = stowRepository.search(criteria)
        .stream()
        .findFirst();

    if (stowResults.isPresent()) {
      val stow = stowResults.get();
      val r = checkStowScanned(stow, uid);

      if (r <= 0) return r;
      if (!stowBelongsToWac(stow, wac.getWacId())) return 3;

      val shouldAssign = isMobile && wac.getMechanizedFlag().equalsIgnoreCase("N");

      stow.setScanInd("Y");
      stow.setModifiedBy(uid);
      if (shouldAssign) stow.setAssignToUser(uid);
      stowRepository.save(stow);

      return 1;
    }
    return -3;
  }

  @Override
  @Transactional
  public void deAssignAllStowsForUser(Integer userId) {
    val criteria = StowSearchCriteria.builder()
        .assignedUserId(userId)
        .build();

    search(criteria).forEach(stow -> {
      stow.setAssignToUser(null);
      update(stow);
    });
  }

  @Override
  @Transactional
  public void delete(Stow stow) {
    stowRepository.delete(stow);
  }

  /**
   * Logic to check and avoid already scanned in stows.
   */
  private Integer checkStowScanned(Stow stow, Integer uid) {
    val scanInd = stow.getScanInd();

    val pick = stow.getPick();

    val status = stow.getStatus();
    val pickStatus = pick != null ? pick.getStatus() : "PICKED"; //if no pick status found, set to PICKED to bypass the rewarehouse check.
    // StowingAmImpl line 132 and 145 is original location for comparison.
    val assignToUser = stow.getAssignToUser();

    // Check if already scanned
    if (scanInd.equalsIgnoreCase("Y")) {
      if (status.equals("STOW BYPASS2") || status.equals("STOW LOSS") || status.equals("STOWLOSS47")) return -4;
      else return 0;
    }
    // Check for a rewarehouse that is not picked yet
    else if (!"PICKED".equalsIgnoreCase(pickStatus)) {
      return -1;
    }
    else {
      // Check if already assigned to handheld user.
      if (assignToUser != null && !assignToUser.equals(uid)) return -2;
      else return 1;
    }
  }

  private List<Stow> assignedStowsByMechFlag(StowSearchCriteria criteria, String mechFlag) {
    switch (mechFlag) {
      case "V":
        return criteriaForMechFlagV(criteria);
      case "H":
        return criteriaForMechFlagH(criteria);
      case "N":
        return criteriaForMechFlagN(criteria);
      default:
        throw new StratisRuntimeException(("Invalid Mech Flag identified"));
    }
  }

  private boolean stowBelongsToWac(Stow stow, Integer wac) {
    return stow.getLocation().getWac().getWacId() == wac;
  }

  private List<Stow> criteriaForMechFlagV(StowSearchCriteria criteria) {
    criteria.setSides(Collections.singletonList("A"));
    criteria.setSort("location.locLevel, location.bay, location.slot", SortOrder.ASC);

    return search(criteria);
  }

  private List<Stow> criteriaForMechFlagH(StowSearchCriteria criteria) {
    criteria.setSides(Collections.singletonList("A"));
    criteria.setSort("location.side, location.bay, location.locLevel, location.slot", SortOrder.ASC);

    val sideAStows = search(criteria);

    criteria.setSides(Collections.singletonList("B"));

    val sideBStows = search(criteria);

    return SMVUtility.mergeLists(sideAStows, sideBStows);
  }

  private List<Stow> criteriaForMechFlagN(StowSearchCriteria criteria) {
    criteria.setSort(sortBuilder.getSortString(LocationSortBuilder.QueryType.HIBERNATE_LOCATION), SortOrder.ASC);

    return search(criteria);
  }
}
