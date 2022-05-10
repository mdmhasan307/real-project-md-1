package mil.usmc.mls2.stratis.core.manager;

import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Stow;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StowingListManager {

  private static Map<Integer, List<Stow>> stowsMap = new HashMap<>();

  private StowingListManager() {}

  /**
   * Note, this currently ignores if there is already an assigned list of stows to a user.  it just overwrites it with the new list to assign.
   * Technically this should be ok, as the way stows are selected to be assigned is any not assigned, or already assigned to a user.  So if the
   * user started the stowing process again they should get the same stows, plus potentially more.  So what gets overwritten doesn't matter.
   */
  public static void assignStowsToUser(Integer userId, List<Stow> stows) {
    stowsMap.put(userId, stows);
  }

  /**
   * Return how many stows are assigned to the user.
   * This is helpful for controlling how many stows can be processed.
   */
  public static Integer getStowCountForUser(Integer userId){
    return getAssignedStowsForUser(userId).size();
  }

  /**
   * Returns the next stow for the user to start working.
   */
  public static Stow getNextStowForUser(Integer userId) {
    val stows = getAssignedStowsForUser(userId);
    if (!CollectionUtils.isEmpty(stows)) {
      return stows.get(0);
    }
    return null;
  }

  /**
   * This assumes that the user is already working a stow, and needs to get it.  It will throw an error if the stow isn't found.  This is by
   * design, as if they are working a stow, and it can't be found here something happened.
   */
  public static Stow getCurrentStowForUser(Integer userId, Integer stowId) {
    return stowsMap.get(userId).stream().filter(x -> x.getStowId().equals(stowId)).findFirst()
        .orElseThrow(() -> new StratisRuntimeException("Stow not found"));
  }

  /**
   * Removes the stow from the list of assigned stows for the user.
   */
  public static void removeStowFromUser(Integer userId, Integer stowId) {
    val stows = getAssignedStowsForUser(userId);
    if (!CollectionUtils.isEmpty(stows)) {
      stows.removeIf(x -> x.getStowId().equals(stowId));
    }
  }

  /**
   * Removes all stows from the user.  This should be triggered on logout, and session timeout.
   */
  public static void removeAllStowsFromUser(Integer userId) {
    stowsMap.remove(userId);
  }

  public static boolean hasNext(Integer userId) {
    val stows = getAssignedStowsForUser(userId);
    return (!CollectionUtils.isEmpty(stows));
  }

  private static List<Stow> getAssignedStowsForUser(Integer userId) {
    return stowsMap.get(userId);
  }
}
