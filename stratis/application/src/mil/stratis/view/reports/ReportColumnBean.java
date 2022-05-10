package mil.stratis.view.reports;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReportColumnBean implements Serializable {

  private String wac;
  private String pickLocation;
  private String packingStation;
  private String column;
  private String pac;
  private String aac;
  private String niin;
  private String documentNumber;
  private String pin;
  private String status;
  private String pickQuantity;
  private String scn;
  private String ui;
  private String supplementaryAddress;
  private String pickedBy;
  private String datePicked;
  private String packedBy;
  private String datePacked;
  private String currentDate;
  private String inventoryType;
  private String expirationDate;
  private String nsnRemark;
  private String sid;
  private String location;
  private String stowedQty;
  private String qtyToBeStowed;
  private String priority;
  private String stowedBy;
  private String assignedToUser;
  private String releasedBy;
  private String cumPosAdj;
  private String cumNegAdj;
  private String interfaceName;
  private String createdDate;
  private String description;
  private String lineNumber;
  private String dataRow;
  private String rcn;
  private String dateReceived;
  private String qtyInducted;
  private String dateStowed;
  private String receivedBy;
  private String qtyBackordered;
  private String qtyReceived;
  private String cc;
  private String nomenclature;
  private String qty;
  private String dom;
  private String serialNumber;
  private String lotControlNumber;
  private String serialLotQty;
  private String issuedFlag;
  private String completedDate;
  private String posAdj;
  private String negAdj;
  private String employee;
  private String price;
  private String priceTotal;
}
