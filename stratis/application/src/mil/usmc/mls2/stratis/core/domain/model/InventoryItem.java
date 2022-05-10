package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class InventoryItem {

  private Integer inventoryItemId;
  private Integer niinId;
  private NiinLocation niinLocation;
  private Integer inventoryId;
  private Integer numCounts;
  private Integer cumNegAdj;
  private Integer cumPosAdj;
  private Integer numCounted;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private String invType;
  private Wac wac;
  private String priority;
  private String status;
  private String transactionType;
  private Integer assignToUser;
  private Integer bypassCount;
  private Integer niinLocQty;
  private Integer completedBy;
  private OffsetDateTime completedDate;
  private Location location;
  private Integer releasedBy;
  private OffsetDateTime releasedDate;

  public void finalizeInventoryCheck(Integer quantity, Integer userId) {
    val adjustment = quantity - niinLocation.getQty();
    val modifiedTime = OffsetDateTime.now();
    numCounted = quantity;
    modifiedBy = userId;
    modifiedDate = modifiedTime;
    status = "INVENTORYREVIEW";
    niinLocQty = niinLocation.getQty();
    cumPosAdj = Math.max(adjustment, 0);
    cumNegAdj = Math.abs(Math.min(adjustment, 0));
    numCounts++;
    completedBy = userId;
    completedDate = modifiedTime;
  }

  public void niinFound(Integer userId) {
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    status = "LOCSURVEYFOUND";
  }

  public void niinNotFound(Integer userId) {
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    status = "LOCSURVEYNOTFOUND";
  }

  public void surveyCompleted(Integer userId) {
    numCounts++;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    status = "LOCSURVEYCOMPLETED";
  }

  public void surveyFailed(Integer userId) {
    numCounts++;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    status = "LOCSURVEYFAILED";
  }

  public void resetSurvey(Integer userId) {
    assignToUser = userId;
    status = "LOCSURVEYPENDING";
  }

  public void assignItem(Integer userId) {
    assignToUser = userId;
  }
}
