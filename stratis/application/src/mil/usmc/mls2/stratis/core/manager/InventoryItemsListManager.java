package mil.usmc.mls2.stratis.core.manager;

import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for Inventory.
 */
public class InventoryItemsListManager {

  private static Map<Integer, List<Integer>> inventoryItemsMap = new HashMap<>();

  private InventoryItemsListManager() {}

  /**
   * Note, this currently ignores if there is already an assigned list of Inventory to a user.  it just overwrites it with the new list to assign.
   * Technically this should be ok, as the way Inventory are selected to be assigned is any not assigned, or already assigned to a user.  So if the
   * user started the inventory process again they should get the same Inventory, plus potentially more.  So what gets overwritten doesn't matter.
   */
  public static void assignInventoryToUser(Integer userId, List<Integer> inventoryItemIds) {
    inventoryItemsMap.put(userId, inventoryItemIds);
  }

  /**
   * Returns the next inventoryItemId for the user to start working.
   */
  public static Integer getNextInventoryItemForUser(Integer userId) {
    val inventoryItems = getAssignedInventoryForUser(userId);
    if (!CollectionUtils.isEmpty(inventoryItems)) {
      return inventoryItems.get(0);
    }
    return null;
  }

  /**
   * This assumes that the user is already working a inventoryItemId, and needs to make sure its still in their list to process.  It will throw an error
   * if the inventoryItemId isn't found.  This is by design, as if they are working a inventoryItemId, and it can't be found here something happened.
   */
  public static Integer getCurrentInventoryItemForUser(Integer userId, Integer inventoryItemId) {
    return inventoryItemsMap.get(userId).stream().filter(x -> x.equals(inventoryItemId)).findFirst()
        .orElseThrow(() -> new StratisRuntimeException("inventoryItemId not found"));
  }

  /**
   * Removes the inventoryItemId from the list of assigned inventoryItemIds for the user.
   */
  public static void removeInventoryItemFromUser(Integer userId, Integer inventoryItemId) {
    val inventoryItems = getAssignedInventoryForUser(userId);
    if (!CollectionUtils.isEmpty(inventoryItems)) {
      inventoryItems.removeIf(x -> x.equals(inventoryItemId));
    }
  }

  /**
   * Removes all inventoryItemIds from the user.  This should be triggered on logout, and session timeout.
   */
  public static void removeAllInventoryFromUser(Integer userId) {
    inventoryItemsMap.remove(userId);
  }

  public static boolean hasNext(Integer userId) {
    val inventoryItems = getAssignedInventoryForUser(userId);
    return (!CollectionUtils.isEmpty(inventoryItems));
  }

  private static List<Integer> getAssignedInventoryForUser(Integer userId) {
    return inventoryItemsMap.get(userId);
  }
}
