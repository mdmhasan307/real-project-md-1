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
public class RefMhif {

  //MHIF - Master Header Information File
  private int refMhifId;
  private String primeFsc;
  private String primeNiin;
  private String primeSmic;
  private String recordFsc;
  private String recordNiin;
  private String recordSmic;
  private String unitOfIssue;
  private String storesAccountCode;
  private String managementEchelonCode;
  private String shelfLifeCode;
  private Integer unitPrice;
  private String recoverabilityCode;
  private String itemNameNomenclature;
  private String physicalSecurityCode;
  private String phraseCode;
  private String pmic;
  private String controlledItemCode;
  private String materielIdentificationCode;
  private String physicalCategoryCode;
  private String combatEssentiallyCode;
  private String nonsystemItemIndicator;
  private String mimmsManagedCode;
  private String adpec;
  private String substituteNsnCounter;
  private String demilitarizationCode;
  private Integer standardReplacementPrice;
  private String sscric;
  private String jdate;
  private String acquisitionAdviceCode;
  private String serialControlFlag;
  private String lotControlFlag;
  private String ciic;
  private OffsetDateTime timestamp;
}
