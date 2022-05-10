package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

  private int warehouseId;
  private String building;
  private String complex;
  private Integer siteId;
  private String description;
  private String locationDeliveryPrefix;
  private String locationDeliverySuffix;
  private String carrierName;
}
