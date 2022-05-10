package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipment implements Serializable {

  private int equipmentNumber;
  private Integer comPortPrinterId;
  private Integer comPortEquipmentId;
  private Wac wac;
  private String name;
  private String description;
  private Integer warehouseId;
  private Integer currentUserId;
  private String shippingArea;
  private String packingGroup;
  private String hasCubiscan;
  private String printerName;
}
