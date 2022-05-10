package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingConsolidation {

  private Integer packingConsolidationId;
  private Integer createdBy;
  private OffsetDateTime createdDate;
  private Integer modifiedBy;
  private OffsetDateTime modifiedDate;
  private BigDecimal consolidationCube;
  private BigDecimal consolidationWeight;
  private String closedCarton;
  private String packLocationBarcode;
  private Integer packingStationId;
  private Integer packColumn;
  private Integer packLevel;
  private Integer numberOfIssues;
  private Customer customer;
  private String consolidationBarcode;
  private String partialRelease;
  private String issuePriorityGroup;
  private Integer packedBy;
  private OffsetDateTime packedDate;
  private String securityMarkClass;

  public void removeIssueDataForPick(BigDecimal cube, BigDecimal weight, Integer userId) {
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    if (numberOfIssues > 0) numberOfIssues--; //only allow number of issues to decrease to 0.
    consolidationCube = consolidationCube.subtract(cube).max(new BigDecimal(0)); //only allow consolidationCube to decrease to 0.
    consolidationWeight = consolidationWeight.subtract(weight).max(new BigDecimal(0)); //only allow consolidationWeight to decrease to 0.
  }

  public void addPackingConsolidationData(BigDecimal cube, BigDecimal weight, Integer userId) {
    modifiedBy = userId;
    modifiedDate = OffsetDateTime.now();
    consolidationCube = consolidationCube.add(cube);
    consolidationWeight = consolidationWeight.add(weight);
  }

  public void updateIdFromDb(Integer id) {
    packingConsolidationId = id;
  }
}
