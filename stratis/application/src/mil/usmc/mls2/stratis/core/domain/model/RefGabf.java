package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefGabf {

  private Integer refGabfId;
  private String primeNationalStockNumber;
  private String recordFsc;
  private String recordNiin;
  private String recordSmic;
  private String activityAddressCode;
  private String unitOfIssue;
  private Double unitPrice;
  private String storesAccountCount;
  private String materielIdentificationCode;
  private String controlledItemCode;
  private String nonsystemItemIndicator;
  private String phraseCode;
  private String familyRelationCode;
  private String managementEchelonCode;
  private String provisioningDate;
  private String cldRequisitioningObjective;
  private String reorderPoint;
  private String requisitioningObjective;
  private String onHandOpStockServiceable;
  private String dueOperatingStock;
  private String backOrderQuantity;
  private String requirementCode;
  private String operationCode;
  private String fixedLevel;
  private String thirtyDayUsage;
  private String floatRequisitionObjective;
  private String excessOnHand;
  private String managementValueFactor;
  private String onHandUnserviceable;
  private String provisioningOnHand;
  private String provisioningDue;
  private String numberOfFirstReceipts;
  private String daysOfFirstReceipts;
  private String supplySourceCode;
  private String freezeCode;
  private String freezeDate;
  private String lastTransactionDate;
  private String analystCode;
  private String totalMountOutAllowance;
  private String mountOutQuantityInBlocks;
  private String mountOutReserved;
  private String specialAllocance;
  private String processed;
  private OffsetDateTime timestamp;
  Integer warReserveQty;
  Integer opcodeReserveQty;
  Integer forecastQty;
  Integer stageQty;
  Integer dockQty;
  Integer roQty;

  public void updateFromFeed(String onHandServiceable, String onHandUnServiceable, String lastTransDate) {
    onHandOpStockServiceable = onHandServiceable;
    onHandUnserviceable = onHandUnServiceable;
    lastTransactionDate = lastTransDate;
  }
}