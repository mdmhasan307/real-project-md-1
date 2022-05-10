package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickingDetailInput implements Serializable {

  private Integer pid;
  private String location;
  private String niin;
  private Integer pickQty;
  private Integer inventoryCount;
  private Integer reInventoryCount;
  private String pin;
  private String mcpx;
  private Integer bypassReason;
  private Integer refuseReason;
  private List<String> serials;
  private boolean partialPickAttempted;

  public void setPin(String pin) {
    this.pin = pin.toUpperCase();
  }

  public static class PickingDetailInputBuilder {

    public PickingDetailInputBuilder pin(String pin) {
      this.pin = pin.toUpperCase();
      return this;
    }
  }
}
