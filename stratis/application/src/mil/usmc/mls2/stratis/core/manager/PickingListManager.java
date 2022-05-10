package mil.usmc.mls2.stratis.core.manager;

import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickingListManager {

  private static Map<Integer, List<Integer>> pickingsMap = new HashMap<>();

  private PickingListManager() {}

  /**
   * Note, this currently ignores if there is already an assigned list of picks to a user.  it just overwrites it with the new list to assign.
   * Technically this should be ok, as the way picks are selected to be assigned is any not assigned, or already assigned to a user.  So if the
   * user started the picking process again they should get the same picks, plus potentially more.  So what gets overwritten doesn't matter.
   */
  public static void assignPicksToUser(Integer userId, List<Integer> pids) {
    pickingsMap.put(userId, pids);
  }

  /**
   * Returns the next pid for the user to start working.
   */
  public static Integer getNextPickForUser(Integer userId) {
    val pickings = getAssignedPicksForUser(userId);
    if (!CollectionUtils.isEmpty(pickings)) {
      return pickings.get(0);
    }
    return null;
  }

  /**
   * This assumes that the user is already working a pid, and needs to make sure its still in their list to process.  It will throw an error
   * if the pid isn't found.  This is by design, as if they are working a pid, and it can't be found here something happened.
   */
  public static Integer getCurrentPickForUser(Integer userId, Integer pid) {
    return pickingsMap.get(userId).stream().filter(x -> x.equals(pid)).findFirst()
        .orElseThrow(() -> new StratisRuntimeException("PID not found"));
  }

  /**
   * Removes the pid from the list of assigned pids for the user.
   */
  public static void removePickFromUser(Integer userId, Integer pid) {
    val pickings = getAssignedPicksForUser(userId);
    if (!CollectionUtils.isEmpty(pickings)) {
      pickings.removeIf(x -> x.equals(pid));
    }
  }

  /**
   * Removes all pids from the user.  This should be triggered on logout, and session timeout.
   */
  public static void removeAllPicksFromUser(Integer userId) {
    pickingsMap.remove(userId);
  }

  public static boolean hasNext(Integer userId) {
    val pickings = getAssignedPicksForUser(userId);
    return (!CollectionUtils.isEmpty(pickings));
  }

  private static List<Integer> getAssignedPicksForUser(Integer userId) {
    return pickingsMap.get(userId);
  }
}
