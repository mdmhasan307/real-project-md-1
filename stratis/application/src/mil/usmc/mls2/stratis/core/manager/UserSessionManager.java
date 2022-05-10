package mil.usmc.mls2.stratis.core.manager;

import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.event.UserSessionTimeoutEvent;
import mil.usmc.mls2.stratis.core.domain.model.Event;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.service.PickingService;
import mil.usmc.mls2.stratis.core.service.StowService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserSessionManager {

  private static StowService staticStowService;
  private static PickingService staticPickingService;
  private static InventoryItemService staticInventoryItemService;
  private static EventService staticEventService;

  public UserSessionManager(StowService stowService, PickingService pickingService, InventoryItemService inventoryItemService, EventService eventService) {
    staticStowService = stowService;
    staticPickingService = pickingService;
    staticInventoryItemService = inventoryItemService;
    staticEventService = eventService;
  }

  private static Map<Integer, List<String>> sessions = new HashMap<>();

  public static void addUserSession(Integer userId, String sessionId) {
    val userSessions = getSessionsForUser(userId);
    userSessions.add(sessionId);
    sessions.put(userId, userSessions);
  }

  public static void logSessionTimeout(UserInfo userInfo, String sessionId) {
    Event event = UserSessionTimeoutEvent.builder()
        .userInfo(userInfo)
        .sessionId(sessionId)
        .build();

    staticEventService.publishEvent(event);
  }

  public static void removeSessionFromUser(int userId, String sessionId) {
    val userSessions = getSessionsForUser(userId);
    userSessions.remove(sessionId);

    if (userSessions.isEmpty()) {
      removeAllSessionsFromUser(userId);
    }
    else {
      sessions.put(userId, userSessions);
    }

    if (!userHasOtherSessions(userId)) {
      //Database deassignment of workload
      staticStowService.deAssignAllStowsForUser(userId);
      staticPickingService.deAssignAllPicksForUser(userId);
      staticInventoryItemService.deAssignAllInventoryItemsForUser(userId); //this will deassign both inventory, and location survey records assigned.
      //Note: Shelf life is not assigned at a DB level.  No need to deassign.

      //Session management clearing of workload
      StowingListManager.removeAllStowsFromUser(userId);
      PickingListManager.removeAllPicksFromUser(userId);
      InventoryItemsListManager.removeAllInventoryFromUser(userId);
      InventoryItemLocationSurveyListManager.removeAllLocationSurveyFromUser(userId);
      ShelfLifeListManager.removeAllNiinLocationsFromUser(userId);
    }
  }

  public static void removeAllSessionsFromUser(Integer userId) {
    sessions.remove(userId);
  }

  public static boolean userHasOtherSessions(Integer userId) {
    val sessions = getSessionsForUser(userId);
    return (!CollectionUtils.isEmpty(sessions));
  }

  private static List<String> getSessionsForUser(Integer userId) {
    List<String> userSessions = sessions.get(userId);
    if (CollectionUtils.isEmpty(userSessions)) {
      userSessions = new ArrayList<>();
    }
    return userSessions;
  }
}
