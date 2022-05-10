package mil.stratis.model.datatype.ship;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class ShippingBarcode {

  private int packingConsolidationId = 0;
  private String barcode = "";
  private int customerId = 0;
  private int workstationId = 0;
  private int warehouseId = 0;
  private String shippingArea = "";
  private ArrayList<ShippingItem> items = new ArrayList<>();
  private ShippingItem item = null;

  public void addShippingItem(ShippingItem item) {
    if (item != null) {
      if (items == null)
        items = new ArrayList<>();
      items.add(item);
    }
  }

  public boolean hasShippingItems() {
    return (CollectionUtils.isNotEmpty(items) || item != null);
  }

  /**
   * Returns true if this shipping floor is assigned to another warehouse of than
   * the current warehouse in question
   */
  public boolean isAssignedToAnotherWarehouse(int warehouseId) {
    //* only compare if the warehouseId is set
    if (this.warehouseId > 0 && warehouseId > 0) {
      return (this.warehouseId != warehouseId);
    }
    return false;
  }

  public boolean isAlreadyAssigned() {
    if (hasShippingItems()) {
      if (CollectionUtils.isNotEmpty(items)) {
        boolean assigned = false;
        for (ShippingItem x : items) {
          if (!x.isUnProcessed()) {
            assigned = true;
          }
        }
        return assigned;
      }

      if (item != null) {
        return !item.isUnProcessed();
      }
    }
    return false;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n      Shipping Barcode =====");

    sb.append("\n        Packing Consolidation Id: ");
    sb.append(getPackingConsolidationId());
    sb.append(" - ");
    sb.append(getBarcode());

    sb.append("\n        Customer Id: ");
    sb.append(getCustomerId());

    sb.append("\n        Workstation Id: ");
    sb.append(getWorkstationId());
    sb.append("\n        Shipping Area: ");
    sb.append(getShippingArea());

    if (items != null) {
      for (ShippingItem x : items) {
        sb.append(x);
      }
    }
    sb.append("\n        SINGLE ITEM: ");
    if (item != null)
      sb.append(item);

    return sb.toString();
  }
}
