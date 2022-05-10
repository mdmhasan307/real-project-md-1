package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingManifest {

  private Integer shippingManifestId;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private Integer equipmentNumber;
  private Customer customer;
  private String leadTcn;
  private String manifest;
  private FloorLocation floorLocation;
  private OffsetDateTime manifestDate;
  private String pickedUpFlag;
  private String deliveredFlag;
  private OffsetDateTime pickedUpDate;
  private OffsetDateTime deliveredDate;
  private OffsetDateTime manifestPrintDate;
  private String driver;
  private String modeOfShipment;
  private Integer manifestedBy;
}
