package mil.usmc.mls2.stratis.core.processor;

import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.domain.model.InventoryItemInput;
import mil.usmc.mls2.stratis.core.domain.model.SpaPostResponse;

public interface InventoryItemProcessor {

  void processInventory(InventoryItem item, InventoryItemInput inventoryItemInput, Integer userId);

  void processForNext(SpaPostResponse spaPostResponse, UserInfo user);
}
