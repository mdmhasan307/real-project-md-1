package mil.usmc.mls2.stratis.core.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.DefaultTransactionExecutor;
import mil.usmc.mls2.stratis.core.manager.InventoryItemsListManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.InvSerialLotNumService;
import mil.usmc.mls2.stratis.core.service.InventoryItemService;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class InventoryItemProcessorImpl implements InventoryItemProcessor {

  private final InventoryItemService inventoryItemService;
  private final EquipmentService equipmentService;
  private final DefaultTransactionExecutor transactionExecutor;
  private final InvSerialLotNumService invSerialLotNumService;

  public void processInventory(InventoryItem inventoryItem, InventoryItemInput inventoryItemInput, Integer userId) {
    val inventorySetupService = SpringAdfBindingUtils.getInventorySetupService();

    Runnable saveInventoryItem = () -> {
      if ("Y".equals(inventoryItem.getNiinLocation().getNiinInfo().getSerialControlFlag()))
        processSerials(inventoryItemInput, inventoryItem, userId);

      inventoryItem.finalizeInventoryCheck(inventoryItemInput.getLocationQty(), userId);
      inventoryItemService.save(inventoryItem);
    };

    transactionExecutor.execute("txSaveInventoryItem", saveInventoryItem, e -> {
      throw new StratisRuntimeException("Error saving InventoryItem", e);
    });

    inventorySetupService.setUserId(userId);
    inventorySetupService.verifyInventoryCount(inventoryItem.getInventoryItemId());

    InventoryItemsListManager.removeInventoryItemFromUser(userId, inventoryItem.getInventoryItemId());
  }

  public void processForNext(SpaPostResponse spaPostResponse, UserInfo user) {
    val userId = user.getUserId();
    //If there is a next return to detail to process, otherwise go to the picking home page.
    if (InventoryItemsListManager.hasNext(userId)) {
      spaPostResponse.setRedirectUrl(MappingConstants.INVENTORY_DETAIL);
    }
    else {
      val workstation = equipmentService.findById(user.getWorkstationId()).orElseGet(Equipment::new);
      val wacId = workstation.getWac().getWacId();

      List<String> pendingInventoryStatuses = new ArrayList<>();
      pendingInventoryStatuses.add("INVENTORYPENDING");
      pendingInventoryStatuses.add("LOCSURVEYPENDING");
      val pendingInventoryCriteria = InventoryItemSearchCriteria.builder()
          .wacId(wacId)
          .statuses(pendingInventoryStatuses)
          .build();
      val inventoryCount = inventoryItemService.count(pendingInventoryCriteria);

      spaPostResponse.addNotification("All Inventory has been processed.");
      if (inventoryCount <= 0) {
        spaPostResponse.setRedirectUrl(MappingConstants.SMV_HOME);
      }
      else {
        spaPostResponse.setRedirectUrl(MappingConstants.INVENTORY_HOME);
      }
    }
  }

  private void processSerials(InventoryItemInput input, InventoryItem item, Integer userId) {
    val serials = input.getSerials();
    val invItemId = item.getInventoryItemId();

    val serialLotCriteria = InvSerialLotNumSearchCriteria.builder()
        .inventoryItemId(invItemId)
        .build();

    //find existing invSerialLotNum records, and delete them.
    val matches = invSerialLotNumService.search(serialLotCriteria);
    matches.forEach(invSerialLotNumService::delete);

    //insert new invSerialLotNum records for the serial numbers entered.
    serials.forEach(x -> {
      val invSerial = InvSerialLotNum.builder()
          .inventoryItemId(invItemId)
          .timestamp(OffsetDateTime.now())
          .invDoneFlag("N")
          .niinId(item.getNiinLocation().getNiinInfo().getNiinId())
          .serialNumber(x)
          .cc(item.getNiinLocation().getCc())
          .expirationDate(item.getNiinLocation().getExpirationDate())
          .qty(1)
          .locationId(item.getLocation().getLocationId())
          .build();
      invSerialLotNumService.save(invSerial);
    });
  }
}
