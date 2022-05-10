package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import org.apache.commons.lang.StringUtils;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefMats {

  private Integer refMatsId;
  private String documentIdentifier;
  private String routingIdentifierFrom;
  private String mediaAndStatusCode;
  private String fsc;
  private String niin;
  private String unitOfIssue;
  private Integer transactionQuantity;
  private String documentNumber;
  private String demandSuffixCode;
  private String supplementaryAddress;
  private String signalCode;
  private String fundCode;
  private String distributionCode;
  private String projectCode;
  private String issuePriorityDesignator;
  private String requiredDeliveryDate;
  private String adviceCode;
  private String routingIdentifierTo;
  private String conditionCode;
  private String shipToAddress1;
  private String shipToAddress2;
  private String shipToAddress3;
  private String shipToAddress4;
  private String eroNumber;
  private Double unitPrice;
  private String shipToAddressCity;
  private String shipToAddressState;
  private String shipToAddressZipCode;
  private String shipToAddressCountry;
  private String status;
  private OffsetDateTime timestamp;
  private String disposalCode;
  private String ron;
  private String demilCode;

  public boolean isValidConditionCode() {
    return !(StringUtils.isEmpty(conditionCode) && documentIdentifier.equalsIgnoreCase("A5J"));
  }
}