package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "SITE_INFO")
@EqualsAndHashCode(of = "siteId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteInfoEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_info_generator")
  @SequenceGenerator(name = "site_info_generator", sequenceName = "site_info_id_seq", allocationSize = 1)
  @Column(name = "SITE_ID")
  private Integer siteId;

  @Column(name = "SUPPLY_CENTER_NAME")
  private String supplyCenterName;

  @Column(name = "CITY")
  private String city;

  @Column(name = "GCSS_MC")
  private String gcssMc;

  @Column(name = "INTERFACES_ON")
  private String interfacesOn;

  @Column(name = "GCSSMC_LOG_CLEAR")
  private Integer gcssmcLogClear;

  @Column(name = "REF_LOG_CLEAR")
  private Integer refLogClear;

  @Column(name = "ROUTING_ID")
  private String routingId;

  @Column(name = "AAC")
  private String aac;

  @Column(name = "issue_cube_threshold")
  private Double issueCubeThreshold;

  @Column(name = "issue_weight_threshold")
  private Double issueWeightThreshold;

  @Column(name = "packing_bypass")
  private String packingBypass;

  @Column(name = "mhif_range")
  private Integer mhifRange;

  @Column(name = "last_imported_niin_id")
  private Integer lastImportedNiinId;

  @Column(name = "SITE_TIMEZONE")
  private String siteTimezone;

  @Column(name = "STATE")
  private String state;

  @Column(name = "ZIP_CODE")
  private String zipCode;

  @Column(name = "CONUS_OCUNUS_FLAG")
  private String conusOcunusFlag;

  @Column(name = "BULK_THRESHOLD")
  private Integer bulkThreshold;

  @Column(name = "SINGLE_ITEM_CUBE")
  private Integer singleItemCube;

  @Column(name = "SINGLE_ITEM_WEIGHT")
  private Integer singleItemWeight;

  @Column(name = "GENERATE_AS2")
  private String generateAs2;

  @Column(name = "INVENTORY_THRESHOLD_VALUE")
  private Integer inventoryThresholdValue;

  @Column(name = "INVENTORY_THRESHOLD_COUNT")
  private Integer inventoryThresholdCount;

  @Column(name = "PACKING_CONSOL_SCAN_COUNT")
  private Integer packingConsolScanCount;

  @Column(name = "SHUTDOWN_MESSAGE")
  private String shutdownMessage;

  @Column(name = "DOC_SERIAL_NUMBER")
  private Integer docSerialNumber;

  @Column(name = "CAROUSEL_COUNT")
  private Integer carouselCount;

  @Column(name = "INHIBIT_MEDLOG")
  private String inhibitMedlog;

  @Column(name = "SHUTDOWN_WARNING")
  private String shutdownWarning;

  @Column(name = "SHUTDOWN_USER_ID")
  private Integer shutdownUserId;

  @Column(name = "HELP_URL")
  private String helpUrl;

  @Column(name = "PENDING_STOW_TIME")
  private Integer pendingStowTime;
}