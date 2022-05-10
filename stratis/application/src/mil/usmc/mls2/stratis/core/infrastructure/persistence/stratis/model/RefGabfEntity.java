package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "REF_GABF")
@EqualsAndHashCode(of = "refGabfId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefGabfEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_gabf_generator")
  @SequenceGenerator(name = "ref_gabf_generator", sequenceName = "ref_gabf_id_seq", allocationSize = 1)
  @Column(name = "REF_GABF_ID")
  private Integer refGabfId;

  @Column(name = "PRIME_NATIONAL_STOCK_NUMBER")
  private String primeNationalStockNumber;

  @Column(name = "RECORD_FSC")
  private String recordFsc;

  @Column(name = "RECORD_NIIN")
  private String recordNiin;

  @Column(name = "RECORD_SMIC")
  private String recordSmic;

  @Column(name = "ACTIVITY_ADDRESS_CODE")
  private String activityAddressCode;

  @Column(name = "UNIT_OF_ISSUE")
  private String unitOfIssue;

  @Column(name = "UNIT_PRICE")
  private Double unitPrice;

  @Column(name = "STORES_ACCOUNT_COUNT")
  private String storesAccountCount;

  @Column(name = "MATERIEL_IDENTIFICATION_CODE")
  private String materielIdentificationCode;

  @Column(name = "CONTROLLED_ITEM_CODE")
  private String controlledItemCode;

  @Column(name = "NONSYSTEM_ITEM_INDICATOR")
  private String nonsystemItemIndicator;

  @Column(name = "PHRASE_CODE")
  private String phraseCode;

  @Column(name = "FAMILY_RELATION_CODE")
  private String familyRelationCode;

  @Column(name = "MANAGEMENT_ECHELON_CODE")
  private String managementEchelonCode;

  @Column(name = "PROVISIONING_DATE")
  private String provisioningDate;

  @Column(name = "CLD_REQUISITIONING_OBJECTIVE")
  private String cldRequisitioningObjective;

  @Column(name = "REORDER_POINT")
  private String reorderPoint;

  @Column(name = "REQUISITIONING_OBJECTIVE")
  private String requisitioningObjective;

  @Column(name = "ON_HAND_OP_STOCK_SERVICEABLE")
  private String onHandOpStockServiceable;

  @Column(name = "DUE_OPERATING_STOCK")
  private String dueOperatingStock;

  @Column(name = "BACK_ORDER_QUANTITY")
  private String backOrderQuantity;

  @Column(name = "REQUIREMENT_CODE")
  private String requirementCode;

  @Column(name = "OPERATION_CODE")
  private String operationCode;

  @Column(name = "FIXED_LEVEL")
  private String fixedLevel;

  @Column(name = "THIRTY_DAY_USAGE")
  private String thirtyDayUsage;

  @Column(name = "FLOAT_REQUISITION_OBJECTIVE")
  private String floatRequisitionObjective;

  @Column(name = "EXCESS_ON_HAND")
  private String excessOnHand;

  @Column(name = "MANAGEMENT_VALUE_FACTOR")
  private String managementValueFactor;

  @Column(name = "ON_HAND_UNSERVICEABLE")
  private String onHandUnserviceable;

  @Column(name = "PROVISIONING_ON_HAND")
  private String provisioningOnHand;

  @Column(name = "PROVISIONING_DUE")
  private String provisioningDue;

  @Column(name = "NUMBER_OF_FIRST_RECEIPTS")
  private String numberOfFirstReceipts;

  @Column(name = "DAYS_OF_FIRST_RECEIPTS")
  private String daysOfFirstReceipts;

  @Column(name = "SUPPLY_SOURCE_CODE")
  private String supplySourceCode;

  @Column(name = "FREEZE_CODE")
  private String freezeCode;

  @Column(name = "FREEZE_DATE")
  private String freezeDate;

  @Column(name = "LAST_TRANSACTION_DATE")
  private String lastTransactionDate;

  @Column(name = "ANALYST_CODE")
  private String analystCode;

  @Column(name = "TOTAL_MOUNT_OUT_ALLOWANCE")
  private String totalMountOutAllowance;

  @Column(name = "MOUNT_OUT_QUANTITY_IN_BLOCKS")
  private String mountOutQuantityInBlocks;

  @Column(name = "MOUNT_OUT_RESERVED")
  private String mountOutReserved;

  @Column(name = "SPECIAL_ALLOCANCE")
  private String specialAllocance;

  @Column(name = "PROCESSED")
  private String processed;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "WAR_RESERVE_QTY")
  private Integer warReserveQty;

  @Column(name = "OPCODE_RESERVE_QTY")
  private Integer opcodeReserveQty;

  @Column(name = "FORECAST_QTY")
  private Integer forecastQty;

  @Column(name = "STAGE_QTY")
  private Integer stageQty;

  @Column(name = "DOCK_QTY")
  private Integer dockQty;

  @Column(name = "RO_QTY")
  private Integer roQty;
}