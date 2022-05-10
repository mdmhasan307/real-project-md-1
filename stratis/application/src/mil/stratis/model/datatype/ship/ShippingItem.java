package mil.stratis.model.datatype.ship;

import lombok.Data;
import lombok.NoArgsConstructor;
import mil.stratis.common.util.Util;

@Data
@NoArgsConstructor
public class ShippingItem {

  private String tcn = "";
  private String scn = "";
  private int shippingId = 0;
  private int shippingManifestId = 0;
  private int workstationId = 0;
  private String lastReviewDate = "";
  private int packingConsolidationId = 0;
  private String ldcon = "";
  private String leadTcn = "";
  private String floorLocation = "";
  private int floorLocationId = 0;
  private String aac = "";
  private int customerId = 0;
  private String manifestDate = "";
  private String manifestPrintDate = "";

  public boolean isUnProcessed() {
    return (shippingManifestId == 0);
  }

  public boolean isEmpty() {
    return Util.isEmpty(tcn);
  }

  public String toString() {
    return "\n        Shipping Item =====" +
        "\n          TCN: " +
        getTcn() +
        "\n          Shipping Id: " +
        getShippingId() +
        "\n          Shipping Manifest Id: " +
        getShippingManifestId() +
        "\n          SCN: " +
        getScn() +
        "\n          Packing Consolidation Id: " +
        getPackingConsolidationId() +
        "\n          Last Review Date: " +
        getLastReviewDate() +
        "\n          Lead TCN: " +
        getLeadTcn() +
        "\n          Floor Location: " +
        getFloorLocationId() +
        " - " +
        getFloorLocation() +
        "\n          Workstation Id: " +
        getWorkstationId() +
        "\n          LDCON: " +
        getLdcon() +
        "\n          Manifest Date: " +
        getManifestDate() +
        "\n          Manifest Print Date: " +
        getManifestPrintDate() +
        "\n          AAC: " +
        getCustomerId() +
        " - " +
        getAac();
  }
}
