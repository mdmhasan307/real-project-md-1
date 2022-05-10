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
public class Shipping {

  private Integer shippingId;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private Integer qty;
  private String tcn;
  private ShippingManifest shippingManifest;
  private String scn;
  private PackingConsolidation packingConsolidation;
  private OffsetDateTime lastReviewDate;
  private String callNumber;
  private String shipmentNumber;
  private OffsetDateTime tailgateDate;
  private String lineNumber;
  private Integer billedAmount;
  private Integer equipmentNumber;
  private String securityMarkClass;
}
