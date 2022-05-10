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
public class ReceiptHistory {

  private int rcn;
  private String frustrateCode;
  private String frustrateLocation;
  private int quantityStowed;
  private int quantityReleased;
  private int quantityMeasured;
  private int quantityInvoiced;
  private int quantityInduced;
  private Integer niinId;
  private String contactNumber;
  private String fundCode;
  private String signalCode;
  private String documentNumber;
  private String documentID;
  private String status;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private String conversionFlag;
  private String routingId;
  private String workStation;
  private String rdd;
  private String supplementaryAddress;
  private String consignee;
  private String dodDistCode;
  private String rpd;
  private String partialShipmentIndicator;
  private String tracabilityNumber;
  private String classCommoditNumber;
  private String shipedFrom;
  private String cc;
  private String projectCode;
  private String pc;
  private String cognizanceCode;
  private String mechNonMechFlag;
  private String ration;
  private String suffix;
  private String shelfLifeCode;
  private Integer weight;
  private Integer length;
  private Integer height;
  private Integer width;
  private String ui;
  private Integer price;
  private String fsc;
  private String partNumber;
  private String serialNumber;
  private Integer quantityBackordered;
  private String userId;
  private String transaction;
  private OffsetDateTime timestamp;
  private String ri;
  private Integer packingConsolidationId;
  private Integer cube;
  private String oldRcn;
  private String niin;
  private String partialShipment;
  private int qtyStowloss;
  private String overageFlag;
  private String shortageFlag;
}
