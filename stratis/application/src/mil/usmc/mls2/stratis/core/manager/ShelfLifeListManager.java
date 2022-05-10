package mil.usmc.mls2.stratis.core.manager;

import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for Shelf Life.
 */
public class ShelfLifeListManager {

  private static Map<Integer, List<Integer>> niinLocationsMap = new HashMap<>();

  private ShelfLifeListManager() {}

  /**
   * Note, this currently ignores if there is already an assigned list of Niin Locations to a user.  it just overwrites it with the new list to assign.
   * Technically this should be ok, as the way Niin Locations are selected to be assigned is any not assigned, or already assigned to a user.  So if the
   * user started the shelf life process again they should get the same Niin locations, plus potentially more.  So what gets overwritten doesn't matter.
   */
  public static void assignNiinLocationsToUser(Integer userId, List<Integer> niinLocationIds) {
    niinLocationsMap.put(userId, niinLocationIds);
  }

  /**
   * Returns the next niinLocationId for the user to start working.
   */
  public static Integer getNextNiinLocationForUser(Integer userId) {
    val niinLocations = getAssignedNiinLocationsForUser(userId);
    if (!CollectionUtils.isEmpty(niinLocations)) {
      return niinLocations.get(0);
    }
    return null;
  }

  /**
   * This assumes that the user is already working a niinLocationId, and needs to make sure its still in their list to process.  It will throw an error
   * if the niinLocationId isn't found.  This is by design, as if they are working a niinLocationId, and it can't be found here something happened.
   */
  public static Integer getCurrentNiinLocationForUser(Integer userId, Integer niinLocationId) {
    return niinLocationsMap.get(userId).stream().filter(x -> x.equals(niinLocationId)).findFirst()
        .orElseThrow(() -> new StratisRuntimeException("niin Location Id not found"));
  }

  /**
   * Removes the niinLocationId from the list of assigned niinLocationIds for the user.
   */
  public static void removeNiinLocationFromUser(Integer userId, Integer niinLocationId) {
    val niinLocations = getAssignedNiinLocationsForUser(userId);
    if (!CollectionUtils.isEmpty(niinLocations)) {
      niinLocations.removeIf(x -> x.equals(niinLocationId));
    }
  }

  /**
   * Removes all niinLocationIds from the user.  This should be triggered on logout, and session timeout.
   */
  public static void removeAllNiinLocationsFromUser(Integer userId) {
    niinLocationsMap.remove(userId);
  }

  public static boolean hasNext(Integer userId) {
    val niinLocations = getAssignedNiinLocationsForUser(userId);
    return (!CollectionUtils.isEmpty(niinLocations));
  }

  private static List<Integer> getAssignedNiinLocationsForUser(Integer userId) {
    return niinLocationsMap.get(userId);
  }
}
