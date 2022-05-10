package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemInput implements Serializable {

  private Integer inventoryItemId;
  private String location;
  private String niin;
  private Integer locationQty;
  private Integer reconfirmLocationQty;
  private String serial;

  @Builder.Default
  private List<String> serials = new ArrayList<>();

  private boolean reconfirmQtyRequired;
}
