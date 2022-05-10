package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "receipt_hist")
@EqualsAndHashCode(of = "rcn") //this is not a unique key
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReceiptHistoryEntity implements Serializable {

  @Id
  @Column(name = "RCN")
  private int rcn;

  @Column(name = "FRUSTRATE_CODE")
  private String frustrateCode;

  @Column(name = "FRUSTRATE_LOCATION")
  private String frustrateLocation;

  @Column(name = "QUANTITY_STOWED")
  private int quantityStowed;

  @Column(name = "QUANTITY_RELEASED")
  private int quantityReleased;

  @Column(name = "QUANTITY_MEASURED")
  private int quantityMeasured;

  @Column(name = "QUANTITY_INVOICED")
  private int quantityInvoiced;

  @Column(name = "QUANTITY_INDUCTED")
  private int quantityInduced;

  @Column(name = "NIIN_ID")
  private Integer niinId;

  @Column(name = "CONTRACT_NUMBER")
  private String contactNumber;

  @Column(name = "FUND_CODE")
  private String fundCode;

  @Column(name = "SIGNAL_CODE")
  private String signalCode;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "DOCUMENT_ID")
  private String documentId;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "CONVERSION_FLAG")
  private String conversionFlag;

  @Column(name = "ROUTING_ID")
  private String routingId;

  @Column(name = "WORK_STATION")
  private String workStation;

  @Column(name = "RDD")
  private String rdd;

  @Column(name = "SUPPLEMENTARY_ADDRESS")
  private String supplementaryAddress;

  @Column(name = "CONSIGNEE")
  private String consignee;

  @Column(name = "DOD_DIST_CODE")
  private String dodDistCode;

  @Column(name = "RPD")
  private String rpd;

  @Column(name = "PARTIAL_SHIPMENT_INDICATOR")
  private String partialShipmentIndicator;

  @Column(name = "TRACEABILITY_NUMBER")
  private String tracabilityNumber;

  @Column(name = "CLASS_COMMODITY_NUMBER")
  private String classCommoditNumber;

  @Column(name = "SHIPPED_FROM")
  private String shipedFrom;

  @Column(name = "CC")
  private String cc;

  @Column(name = "PROJECT_CODE")
  private String projectCode;

  @Column(name = "PC")
  private String pc;

  @Column(name = "COGNIZANCE_CODE")
  private String cognizanceCode;

  @Column(name = "MECH_NON_MECH_FLAG")
  private String mechNonMechFlag;

  @Column(name = "RATION")
  private String ration;

  @Column(name = "SUFFIX")
  private String suffix;

  @Column(name = "SHELF_LIFE_CODE")
  private String shelfLifeCode;

  @Column(name = "WEIGHT")
  private Integer weight;

  @Column(name = "LENGTH")
  private Integer length;

  @Column(name = "HEIGHT")
  private Integer height;

  @Column(name = "WIDTH")
  private Integer width;

  @Column(name = "UI")
  private String ui;

  @Column(name = "PRICE")
  private Integer price;

  @Column(name = "FSC")
  private String fsc;

  @Column(name = "PART_NUMBER")
  private String partNumber;

  @Column(name = "SERIAL_NUMBER")
  private String serialNumber;

  @Column(name = "QUANTITY_BACKORDERED")
  private Integer quantityBackordered;

  @Column(name = "USER_ID")
  private String userId;

  @Column(name = "TRANSACTION")
  private String transaction;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;

  @Column(name = "RI")
  private String ri;

  @Column(name = "PACKING_CONSOLIDATION_ID")
  private Integer packingConsolidationId;

  @Column(name = "CUBE")
  private Integer cube;

  @Column(name = "OLD_RCN")
  private String oldRcn;

  @Column(name = "NIIN")
  private String niin;

  @Column(name = "PARTIAL_SHIPMENT")
  private String partialShipment;

  @Column(name = "QTY_STOWLOSS")
  private int qtyStowloss;

  @Column(name = "OVERAGE_FLAG")
  private String overageFlag;

  @Column(name = "SHORTAGE_FLAG")
  private String shortageFlag;
}
