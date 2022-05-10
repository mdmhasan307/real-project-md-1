package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.user.UserInfo;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stow {

  private Integer stowId;
  private String sid;
  private int qtyToBeStowed;
  private Receipt receipt;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private Picking pick;
  private String status;
  private String cancelReason;
  private LocalDate expirationDate;
  private LocalDate dateOfManufacture;
  private Location location;
  private String lotConNum;
  private Integer caseWeightQty;
  private LocalDate packedDate;
  private String serialNumber;
  private Integer stowQty;
  private String scanInd;
  private Integer bypassCount;
  private Integer assignToUser;
  private Integer stowedBy;
  private OffsetDateTime stowedDate;
  private String securityMarkClass;

  public void setQtyToBeStowed(int qty) {
    qtyToBeStowed = qty;
  }

  public void reassginReceipt(Receipt receipt) {
    this.receipt = receipt;
  }

  public void stowStatusChange(Location newLocation, int quantity, int userId) {
    location = newLocation;
    stowQty = quantity;
    modifiedBy = userId;
  }

  public void finalizeStowForReceiptCreation(Location loc, int userId) {
    location = loc;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    if (!StringUtils.isEmpty(serialNumber))
      serialNumber = serialNumber.toUpperCase();
    if (!StringUtils.isEmpty(lotConNum))
      lotConNum = lotConNum.toUpperCase();
    status = "STOW READY";
  }
}
