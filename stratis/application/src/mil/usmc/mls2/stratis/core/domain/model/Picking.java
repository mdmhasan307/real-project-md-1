package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Picking {

  private Integer pid;
  private String scn;
  private String suffixCode;
  private Integer packingConsolidationId;
  private NiinLocation niinLocation;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private Integer qtyPicked;
  private String pin;
  private String status;
  private Integer pickQty;
  private Integer bypassCount;
  private OffsetDateTime timeOfPick;
  private Integer assignToUser;
  private Integer pickedBy;
  private OffsetDateTime packedDate;
  private Integer refusedBy;
  private OffsetDateTime refusedDate;
  private Integer qtyRefused;
  private String refusedFlag;
  private String securityMarkClass;

  public void allocatePick(Integer pickedQty, String issuePin, Integer userId) {
    val currentTime = OffsetDateTime.now();
    timeOfPick = currentTime;
    status = "PICKED";
    qtyPicked = qtyPicked + pickedQty;
    pin = issuePin;
    modifiedBy = userId;
    modifiedDate = currentTime;
    assignToUser = null; //this will deassign the pick once its ben allocated.
  }

  public void resetPick(Integer quantity) {
    status = "PICK READY";
    timeOfPick = null;
    qtyPicked = quantity;
    assignToUser = null; //this will deassign the pick once its reset.
  }

  public void assignPick(Integer userId) {
    assignToUser = userId;
  }

  public void bypassPick(Integer userId) {
    modifiedDate = OffsetDateTime.now();
    modifiedBy = userId;
    bypassCount = 1;
    assignToUser = null; //this will deassign the pick once its bypassed.
    status = "PICK BYPASS1";
  }

  public void confirmBypassPick(Integer userId) {
    modifiedDate = OffsetDateTime.now();
    modifiedBy = userId;
    bypassCount++;
    assignToUser = null; //this will deassign the pick once its bypassed.
    status = "PICK BYPASS2";
  }

  public void refusePick(Integer userId) {
    modifiedDate = OffsetDateTime.now();
    modifiedBy = userId;
    assignToUser = null; //this will deassign the pick once its refused.
    status = "PICK REFUSED";
  }

  public void updateIdFromDb(Integer id) {
    pid = id;
  }

  public void removePackingStation(Integer userId) {
    packingConsolidationId = null;
    modifiedDate = OffsetDateTime.now();
    modifiedBy = userId;
  }

  public void addPackingConsolidationId(Integer packingConsolId, Integer userId) {
    packingConsolidationId = packingConsolId;
    modifiedDate = OffsetDateTime.now();
    modifiedBy = userId;
  }
}
