package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

public class Receipt implements Serializable {

  private int rcn;
  private String frustrateCode;
  private String frustrateLocation;
  private int quantityStowed;
  private int quantityReleased;
  private int quantityMeasured;
  private int quantityInvoiced;
  private int quantityInduced;
  private NiinInfo niinInfo;
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
  private BigDecimal weight;
  private BigDecimal length;
  private BigDecimal height;
  private BigDecimal width;
  private String ui;
  private Double price;
  private String fsc;
  private String partNumber;
  private String serialNumber;
  private Integer quantityBackordered;
  private String ri;
  private Integer packingConsolidationId;
  private BigDecimal cube;
  private String niin;
  private String partialShipment;
  private int qtyStowloss;
  private String overageFlag;
  private String shortageFlag;
  private String securityMarkClass;

  public void addConversionResult(NiinInfo niinInfo) {

    quantityMeasured = 1;
    length = niinInfo.getLength();
    width = niinInfo.getWidth();
    height = niinInfo.getHeight();
    weight = niinInfo.getWeight();
    cube = niinInfo.getCube();

    shelfLifeCode = niinInfo.getShelfLifeCode() != null ? niinInfo.getShelfLifeCode().getShelfLifeCode() : null;
  }

  public void setDetails(ReceivingDetailInput input, Integer userId, String generatedSuffix) {
    quantityInduced = input.getQtyInducted();
    ri = input.getRi();
    cc = input.getCc();
    mechNonMechFlag = input.getBulk();
    status = "RECEIPT DRAFT";
    conversionFlag = "N";
    suffix = generatedSuffix;
    if (rcn == 0) {
      createdBy = userId;
      createdDate = OffsetDateTime.now();
    }
    else {
      modifiedBy = userId;
      modifiedDate = OffsetDateTime.now();
    }
  }

  public void processPickForReceipt(int qtyStowed) {
    quantityStowed = qtyStowed;
    modifiedBy = 1; //hard coded because of stratis reasons
    modifiedDate = OffsetDateTime.now();
  }

  public void convertReceiptUI(int qtyInvoiced, Double priceInput) {
    quantityInvoiced = qtyInvoiced;
    price = priceInput;
  }

  public void stowsCreated(int qtyAddedToStows, int userId) {
    quantityStowed = qtyAddedToStows;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }

  public void completeReceipt(int userId) {
    status = "RECEIPT COMPLETE";
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }

  public void setQuantitiesForD6Copy(int qty) {
    quantityInduced = qty;
    quantityInvoiced = qty;
  }
}
