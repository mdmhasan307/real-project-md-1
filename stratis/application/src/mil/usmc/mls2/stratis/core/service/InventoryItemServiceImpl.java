package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItemSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.domain.repository.InventoryItemRepository;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class InventoryItemServiceImpl implements InventoryItemService {

  private final InventoryItemRepository inventoryItemRepository;
  private final GlobalConstants globalConstants;
  private final LocationSortBuilder sortBuilder;

  @Override
  @Transactional(readOnly = true)
  public Optional<InventoryItem> findById(Integer id) {
    return inventoryItemRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(InventoryItemSearchCriteria criteria) {
    return inventoryItemRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventoryItem> search(InventoryItemSearchCriteria criteria) {
    return inventoryItemRepository.search(criteria);
  }

  @Override
  @Transactional
  public void deAssignAllInventoryItemsForUser(Integer userId) {
    val criteria = InventoryItemSearchCriteria.builder()
        .assignedUserId(userId)
        .build();

    val inventoryItems = search(criteria);

    inventoryItems.forEach(x -> {
      x.assignItem(null);
      inventoryItemRepository.save(x);
    });
  }

  @Override
  @Transactional
  public List<InventoryItem> getInventoryListForProcessing(InventoryItemSearchCriteria criteria, Wac wac) {
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

  @Override
  @Transactional
  public void save(InventoryItem inventoryItem) {
    inventoryItemRepository.save(inventoryItem);
  }

  private List<InventoryItem> getMechFlagVPicks(InventoryItemSearchCriteria criteria) {
    List<String> sides = new ArrayList<>();
    sides.add("A");
    criteria.setSides(sides);
    criteria.setSort("location.locLevel, location.bay, location.slot", SortOrder.ASC); //note these are location fields in inventoryitem.location....

    return search(criteria);
  }

  /**
   * while the database could grab both side A and B at the same time the front end is expecting the order to be an alternating list of side A and B records.
   */
  private List<InventoryItem> getMechFlagHPicks(InventoryItemSearchCriteria criteria) {
    List<String> sides = new ArrayList<>();
    sides.add("A");
    criteria.setSides(sides);
    criteria.setSort("location.bay, location.locLevel, location.slot", SortOrder.ASC); //note these are location fields in inventoryitem.location....

    val sideAResults = search(criteria);

    sides = new ArrayList<>();
    sides.add("B");
    criteria.setSides(sides);

    val sideBResults = search(criteria);

    return SMVUtility.mergeLists(sideAResults, sideBResults);
  }

  private List<InventoryItem> getMechFlagNPicks(InventoryItemSearchCriteria criteria, Integer tasksPerTrip) {
    //we can't limit the database returning, as the sorting is happening here.
    criteria.setMaxItems(tasksPerTrip);

    criteria.setSort("assignToUser, " + sortBuilder.getSortString(LocationSortBuilder.QueryType.HIBERNATE_LOCATION), SortOrder.ASC); //note these are location fields in inventoryitem.location....

    val results = search(criteria);

    boolean unassignedBeforeAssigned = false;
    if (CollectionUtils.isNotEmpty(results)) {
      val firstAssignedUser = results.get(0).getAssignToUser();
      val lastAssignedUser = results.get(results.size() - 1).getAssignToUser();
      if (firstAssignedUser == null && lastAssignedUser != null) {
        unassignedBeforeAssigned = true;
      }
    }

    //Due to a scenario where a user may have items already assigned to them, and items from a previous user extra sorting may be needed.
    //Example User A Logs in and gets assigned records.
    //User B then signs in and gets records.  User B then works some (but not all)
    //User A then logs out deassigning records.
    //User B closes their browser then logs in.  At this point they would get the records assigned to them plus x records before those that were assigned to user A.
    //  The order from the DB is correct in terms of warehouse order, but the users want to work the items they had assigned first.
    //if there are unassigned before assigned, we need to move them to the end.
    if (unassignedBeforeAssigned) {
      return processUnassignedAndAssigned(results, criteria);
    }
    else {
      results.forEach(x -> {
        //item could already be assigned, so don't assign again.
        if (x.getAssignToUser() == null) {
          x.assignItem(criteria.getAssignedUserId());
          inventoryItemRepository.save(x);
        }
      });
      return results;
    }
  }

  private List<InventoryItem> processUnassignedAndAssigned(List<InventoryItem> results, InventoryItemSearchCriteria criteria) {

    List<InventoryItem> notAssigned = new ArrayList<>();
    List<InventoryItem> alreadyAssigned = new ArrayList<>();

    AtomicBoolean assignmentFound = new AtomicBoolean(false);
    results.forEach(x -> {
      //no assignment found yet, so check the current.
      if (!assignmentFound.get()) {
        val currentAssignedId = x.getAssignToUser();
        //set assignmentFound if one is assigned.
        if (currentAssignedId != null) assignmentFound.set(true);
      }
      //now that we have checked for assignment, add to appropriate list.
      if (!assignmentFound.get()) {
        x.assignItem(criteria.getAssignedUserId());
        inventoryItemRepository.save(x);
        notAssigned.add(x);
      }
      else {
        alreadyAssigned.add(x);
      }
    });
    alreadyAssigned.addAll(notAssigned);
    return alreadyAssigned;
  }
}
