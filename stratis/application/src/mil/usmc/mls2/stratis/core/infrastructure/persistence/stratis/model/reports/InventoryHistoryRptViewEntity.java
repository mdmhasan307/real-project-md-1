package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Data
@Entity
@Immutable
@Table(name = "V_INVENTORY_HISTORY_REPORT")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InventoryHistoryRptViewEntity {

  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "COMPLETED_DATE")
  private OffsetDateTime completedDate;

  @Column(name = "COMPLETED_BY")
  private String completedBy;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "NIIN")
  private String niin;

  @Column(name = "NOMENCLATURE")
  private String nomenclature;

  @Column(name = "CC")
  private String cc;

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "LOCATION_LABEL")
  private String locationLabel;

  @Column(name = "CUM_NEG_ADJ")
  private Integer negAdj;

  @Column(name = "CUM_POS_ADJ")
  private Integer posAdj;

  @Column(name = "PRICE")
  private Double price;

  @Column(name = "TOTAL_VALUE")
  private Double totalValue;

  @Column(name = "WAC")
  private String wac;
}
