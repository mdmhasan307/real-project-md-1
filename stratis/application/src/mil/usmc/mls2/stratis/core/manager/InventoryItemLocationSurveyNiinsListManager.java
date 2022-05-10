package mil.usmc.mls2.stratis.core.manager;

import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for Location Survey.
 */
public class InventoryItemLocationSurveyNiinsListManager {

  private static Map<Integer, List<Integer>> locationSurveyNiinIdsMap = new HashMap<>();

  private InventoryItemLocationSurveyNiinsListManager() {}

  /**
   * Note, this currently ignores if there is already an assigned list of niin IDs to a user.  it just overwrites it with the new list to assign.
   */
  public static void assignLocationSurveyNiinIdsToUser(Integer userId, List<Integer> niinIds) {
    locationSurveyNiinIdsMap.put(userId, niinIds);
  }

  /**
   * Returns the next niinId for the user to start working.
   */
  public static Integer getNextLocationSurveyNiinIdForUser(Integer userId) {
    val niins = getAssignedLocationSurveyNiinIdsForUser(userId);
    if (!CollectionUtils.isEmpty(niins)) {
      return niins.get(0);
    }
    return null;
  }

  /**
   * This assumes that the user is already working a niin, and needs to make sure its still in their list to process.  It will throw an error
   * if the niin isn't found.  This is by design, as if they are working a niin, and it can't be found here something happened.
   */
  public static Integer getCurrentLocationSurveyNiinsForUser(Integer userId, Integer niinId) {
    return locationSurveyNiinIdsMap.get(userId).stream().filter(x -> x.equals(niinId)).findFirst()
        .orElseThrow(() -> new StratisRuntimeException("location survey niin ID not found"));
  }

  /**
   * Removes the niin from the list of assigned niins for the user.
   */
  public static void removeLocationSurveyNiinFromUser(Integer userId, Integer niinId) {
    val locationSurveyNiins = getAssignedLocationSurveyNiinIdsForUser(userId);
    if (!CollectionUtils.isEmpty(locationSurveyNiins)) {
      locationSurveyNiins.removeIf(x -> x.equals(niinId));
    }
  }

  /**
   * Removes all niins from the user.  This should be triggered on logout, and session timeout.
   */
  public static void removeAllLocationSurveyNiinIdsFromUser(Integer userId) {
    locationSurveyNiinIdsMap.remove(userId);
  }

  public static boolean hasNext(Integer userId) {
    val locationSurveyNiins = getAssignedLocationSurveyNiinIdsForUser(userId);
    return (!CollectionUtils.isEmpty(locationSurveyNiins));
  }

  private static List<Integer> getAssignedLocationSurveyNiinIdsForUser(Integer userId) {
    return locationSurveyNiinIdsMap.get(userId);
  }
}
