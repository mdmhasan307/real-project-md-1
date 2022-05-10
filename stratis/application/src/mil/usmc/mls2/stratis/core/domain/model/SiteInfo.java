package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteInfo {

  private Integer siteId;
  private String supplyCenterName;
  private String city;
  private String gcssMc;
  private String interfacesOn;
  private Integer gcssmcLogClear;
  private Integer refLogClear;
  private String routingId;
  private String aac;
  private Double issueCubeThreshold;
  private Double issueWeightThreshold;
  private String packingBypass;
  private Integer mhifRange;
  private Integer lastImportedNiinId;
  private String siteTimezone;
  private String state;
  private String zipCode;
  private String conusOcunusFlag;
  private Integer bulkThreshold;
  private Integer singleItemCube;
  private Integer singleItemWeight;
  private String generateAs2;
  private Integer inventoryThresholdValue;
  private Integer inventoryThresholdCount;
  private Integer packingConsolScanCount;
  private String shutdownMessage;
  private Integer docSerialNumber;
  private Integer carouselCount;
  private String inhibitMedlog;
  private String shutdownWarning;
  private Integer shutdownUserId;
  private String helpUrl;
  private Integer pendingStowTime;

  public void updateMhifLastImportedNiinId(Integer newLastImportedNiinId) {
    lastImportedNiinId = newLastImportedNiinId;
  }
}
