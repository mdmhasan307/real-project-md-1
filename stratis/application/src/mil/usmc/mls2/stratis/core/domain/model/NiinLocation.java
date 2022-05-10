package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NiinLocation {

  private Integer niinLocationId;
  private NiinInfo niinInfo;
  private Location location;
  private Integer qty;
  private LocalDate expirationDate;
  private LocalDate dateOfManufacture;
  private String cc;
  private String locked;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private String projectCode;
  private String pc;
  private OffsetDateTime lastInvDate;
  private Integer caseWeightQty;
  private String lotConNum;
  private String serialNumber;
  private LocalDate packedDate;
  private Integer numExtents;
  private Integer numCounts;
  private String underAudit;
  private String oldUi;
  private String nsnRemark;
  private String expRemark;
  private Integer oldQty;
  private String recallFlag;
  private OffsetDateTime recallDate;
  private String securityMarkClass;

  public void surveyAddNiinLocation(NiinInfo niinInfo, Location location, String cc, LocalDate expirationDate, Integer userId) {
    val date = OffsetDateTime.now();
    this.niinInfo = niinInfo;
    this.location = location;
    this.cc = cc;
    this.expirationDate = expirationDate;
    qty = 0;
    createdBy = userId;
    createdDate = date;
    modifiedBy = userId;
    modifiedDate = date;
    locked = "N";
  }

  public void surveyUpdateNiinLocation(Integer userId) {
    val date = OffsetDateTime.now();
    modifiedBy = userId;
    modifiedDate = date;
  }

  public void stowNiin(Integer pickedQty, Integer userId) {
    this.qty = this.qty + pickedQty;
    oldUi = null;
    nsnRemark = "N";
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }

  public void allocatePick(Integer pickedQty, Integer userId) {
    this.qty = this.qty - pickedQty;
    oldUi = null;
    nsnRemark = "N";
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }

  public void repack(Integer userId) {
    oldUi = null;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }

  public void resetUi() {
    oldUi = null;
  }

  public void resetRemark() {
    nsnRemark = "N";
  }

  public void updateExpirationDateForShelfLife(LocalDate newExpirationDate, Integer userId) {
    expRemark = "N";
    expirationDate = newExpirationDate;
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
  }
}
