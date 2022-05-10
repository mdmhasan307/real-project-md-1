package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "ref_mhif")
@EqualsAndHashCode(of = "refMhifId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RefMhifEntity implements Serializable {

  //MHIF - Master Header Information File
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ref_mhif_generator")
  @SequenceGenerator(name = "ref_mhif_generator", sequenceName = "ref_mhif_id_seq", allocationSize = 1)
  @Column(name = "REF_MHIF_ID")
  private int refMhifId;

  @Column(name = "PRIME_FSC")
  private String primeFsc;

  @Column(name = "PRIME_NIIN")
  private String primeNiin;

  @Column(name = "PRIME_SMIC")
  private String primeSmic;

  @Column(name = "RECORD_FSC")
  private String recordFsc;

  @Column(name = "RECORD_NIIN")
  private String recordNiin;

  @Column(name = "RECORD_SMIC")
  private String recordSmic;

  @Column(name = "UNIT_OF_ISSUE")
  private String unitOfIssue;

  @Column(name = "STORES_ACCOUNT_CODE")
  private String storesAccountCode;

  @Column(name = "MANAGEMENT_ECHELON_CODE")
  private String managementEchelonCode;

  @Column(name = "SHELF_LIFE_CODE")
  private String shelfLifeCode;

  @Column(name = "UNIT_PRICE")
  private Integer unitPrice;

  @Column(name = "RECOVERABILITY_CODE")
  private String recoverabilityCode;

  @Column(name = "ITEM_NAME_NOMENCLATURE")
  private String itemNameNomenclature;

  @Column(name = "PHYSICAL_SECURITY_CODE")
  private String physicalSecurityCode;

  @Column(name = "PHRASE_CODE")
  private String phraseCode;

  @Column(name = "PMIC")
  private String pmic;

  @Column(name = "CONTROLLED_ITEM_CODE")
  private String controlledItemCode;

  @Column(name = "MATERIEL_IDENTIFICATION_CODE")
  private String materielIdentificationCode;

  @Column(name = "PHYSICAL_CATEGORY_CODE")
  private String physicalCategoryCode;

  @Column(name = "COMBAT_ESSENTIALLY_CODE")
  private String combatEssentiallyCode;

  @Column(name = "NONSYSTEM_ITEM_INDICATOR")
  private String nonsystemItemIndicator;

  @Column(name = "MIMMS_MANAGED_CODE")
  private String mimmsManagedCode;

  @Column(name = "ADPEC")
  private String adpec;

  @Column(name = "SUBSTITUTE_NSN_COUNTER")
  private String substituteNsnCounter;

  @Column(name = "DEMILITARIZATION_CODE")
  private String demilitarizationCode;

  @Column(name = "STANDARD_REPLACEMENT_PRICE")
  private Integer standardReplacementPrice;

  @Column(name = "SSC_RIC")
  private String sscric;

  @Column(name = "JDATE")
  private String jdate;

  @Column(name = "ACQUISITION_ADVICE_CODE")
  private String acquisitionAdviceCode;

  @Column(name = "SERIAL_CONTROL_FLAG")
  private String serialControlFlag;

  @Column(name = "LOT_CONTROL_FLAG")
  private String lotControlFlag;

  @Column(name = "CIIC")
  private String ciic;

  @Column(name = "TIMESTAMP")
  private OffsetDateTime timestamp;
}
