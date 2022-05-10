package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NiinInfo implements Serializable {

  private Integer niinId;
  private String niin;
  private String nomenclature;
  private BigDecimal cube;
  private int price;
  private OffsetDateTime activityDate;
  private String tamcn;
  private String supplyClass;
  private String typeOfMaterial;
  private String cognizanceCode;
  private String partNumber;
  private String ui;
  private String cageCode;
  private String fsc;
  private RefSlc shelfLifeCode;
  private BigDecimal weight;
  private BigDecimal length;
  private BigDecimal width;
  private BigDecimal height;
  private Integer shelfLifeExtension;
  private String scc;
  private String inventoryThreshold;
  private Integer sassyBalance;
  private Integer roThreshold;
  private String smic;
  private String serialControlFlag;
  private String lotControlFlag;
  private String newNsn;
  private Integer createdBy;
  private Integer modifiedBy;
  private OffsetDateTime lastMhifUpdateDate;
  private String demilCode;
  private String securityMarkClass;

  public void updateNiinInfoFromGcss(String fsc, String nomenclature, int price, String serialControlFlag, String lotControlFlag,
                                     String unitOfIssue, String smic, String scc, Integer shelfLifeExtension, String demilCode, RefSlc refSlc) {
    this.fsc = fsc;
    this.nomenclature = nomenclature;
    this.price = price;
    this.serialControlFlag = serialControlFlag;
    this.lotControlFlag = lotControlFlag;
    this.ui = unitOfIssue;
    this.smic = smic;
    this.scc = scc;
    this.shelfLifeExtension = shelfLifeExtension;
    this.demilCode = demilCode;
    this.shelfLifeCode = refSlc;
    lastMhifUpdateDate = OffsetDateTime.now();
  }
}
