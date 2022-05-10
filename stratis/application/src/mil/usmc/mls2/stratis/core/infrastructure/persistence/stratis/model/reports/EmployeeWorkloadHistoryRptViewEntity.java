package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Immutable
@Table(name = "V_EMPLOYEE_WORKLOAD_HIST")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeWorkloadHistoryRptViewEntity {

  @EmbeddedId
  @AttributeOverride(name = "user", column = @Column(name = "USER_FULL_NAME"))
  @AttributeOverride(name = "transDate", column = @Column(name = "TRANS_DATE"))
  private EmployeeWorkloadHistoryKey id;

  @Column(name = "RECEIPT_COUNT")
  private Integer receiptCount;

  @Column(name = "RECEIPTS_DOLLAR_VALUE")
  private Double receiptsDollarValue;

  @Column(name = "STOW_COUNT")
  private Integer stowCount;

  @Column(name = "STOWS_DOLLAR_VALUE")
  private Double stowDollarValue;

  @Column(name = "PICK_COUNT")
  private Integer pickCount;

  @Column(name = "PICKS_DOLLAR_VALUE")
  private Double pickDollarValue;

  @Column(name = "PACK_CONSOLIDATION_COUNT")
  private Integer packConsolidationCount;

  @Column(name = "PACKS_DOLLAR_VALUE")
  private Double packDollarValue;

  @Column(name = "INV_ITEM_COUNT")
  private Integer inventoryItemCount;

  @Column(name = "INVS_DOLLAR_VALUE")
  private Double inventoryDollarValue;

  @Column(name = "OTHER_COUNT")
  private Integer otherCount;

  @Data
  @Embeddable
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EmployeeWorkloadHistoryKey implements Serializable {

    private String user;
    private OffsetDateTime transDate;
  }
}
