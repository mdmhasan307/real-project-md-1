package mil.stratis.model.datatype.ship;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class ShippingContainer {

  private int shippingManifestId = 0;
  private String ldcon = "";
  private String leadTcn = "";
  private String floorLocation = "";
  private int floorLocationId = 0;
  private String aac = "";
  private int customerId = 0;
  private String manifestDate = "";
  private String manifestPrintDate = "";

  public boolean isManifested() {
    return !StringUtils.isEmpty(manifestDate);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n  Shipping Container =====");
    sb.append("\n    Lead TCN: ");
    sb.append(getLeadTcn());

    sb.append("\n    Floor Location: ");
    sb.append(getFloorLocationId());
    sb.append(" - ");
    sb.append(getFloorLocation());

    sb.append("\n    LDCON: ");
    sb.append(getLdcon());
    sb.append("\n    Manifest Date: ");
    sb.append(getManifestDate());
    sb.append("\n    Manifest Print Date: ");
    sb.append(getManifestPrintDate());

    sb.append("\n    AAC: ");
    sb.append(getCustomerId());
    sb.append(" - ");
    sb.append(getAac());

    return sb.toString();
  }
}
