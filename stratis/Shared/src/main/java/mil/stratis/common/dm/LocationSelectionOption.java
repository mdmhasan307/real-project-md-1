package mil.stratis.common.dm;

import lombok.Data;

@Data
public class LocationSelectionOption {

  private Integer locationId;
  private String locationLabel;
  private Integer locationQuantity;

  public String getLocationOption() {
    return String.format("%s (Qty-%d)", this.locationLabel, this.locationQuantity);
  }
}
