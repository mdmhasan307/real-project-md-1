package mil.stratis.model.datatype.ship;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;

@Slf4j
@Data
@NoArgsConstructor
public class ShippingFloor {

  private int floorLocationId = 0;
  private String floorLocation = "";
  private String shippingArea = "";
  private String inUseFlag = "";
  private int workstationId = 0;
  private int warehouseId = 0;
  private ArrayList<ShippingContainer> containers = new ArrayList<>();
  private ShippingContainer container = null;
  private boolean error = false;

  public boolean isInUse() {
    return (inUseFlag.equals("Y"));
  }

  public void addShippingContainer(ShippingContainer container) {
    if (container != null) {
      if (containers == null)
        containers = new ArrayList<>();
      containers.add(container);
    }
  }

  public boolean hasShippingContainers() {
    return (CollectionUtils.isNotEmpty(containers) || container != null);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\nShipping Floor =====");

    sb.append("\n  Floor Location: ");
    sb.append(getFloorLocationId());
    sb.append(" - ");
    sb.append(getFloorLocation());

    sb.append("\n  Warehouse Id: ");
    sb.append(getWarehouseId());
    sb.append("\n  Workstation Id: ");
    sb.append(getWorkstationId());
    sb.append("\n  Shipping Area: ");
    sb.append(getShippingArea());

    if (containers != null) {
      for (ShippingContainer c : containers) {
        sb.append(c);
      }
    }
    sb.append("\n  SINGLE CONTAINER: ");
    if (container != null)
      sb.append(container);

    return sb.toString();
  }

  public boolean hasError() {
    return error;
  }
}
