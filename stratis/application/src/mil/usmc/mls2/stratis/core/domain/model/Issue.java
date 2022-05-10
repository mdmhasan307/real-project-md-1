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
public class Issue {

  private String scn;
  private String documentId;
  private String documentNumber;
  private Integer qty;
  private String issueType;
  private String issuePriorityDesignator;
  private String issuePriorityGroup;
  private Customer customer;
  private Integer niinId;
  private String status;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private Integer packingConsolidationId;
  private String suffix;
  private String rdd;
  private String supplementaryAddress;
  private String fundCode;
  private String mediaAndStatusCode;
  private String routingIdFrom;
  private String signalCode;
  private String distributionCode;
  private String projectCode;
  private String adviceCode;
  private String eroNumber;
  private String cc;
  private String dateBackOrdered;
  private String costJon;
  private Integer qtyIssued;
  private String rcn;
  private Integer relToShippingBy;
  private OffsetDateTime relToShippingDate;
  private String disposalCode;
  private String ron;
  private String demilCode;
  private String securityMarkClass;

  public void convertWalkthrough(Integer userId) {
    documentId = "A5A";
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }

  public void removePackingConsolidationId(Integer userId) {
    packingConsolidationId = null;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }
}
