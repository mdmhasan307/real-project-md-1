package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Getter
@Entity
@Immutable
@Table(name = "V_SUMOUT_SPOOL_DETAIL_GCSS")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InterfaceTransactionRptViewEntity {

  @Id
  @Column(name = "ID")
  private Integer id;

  @Column(name = "TIMESTAMP")
  private String timestamp;

  @Column(name = "GCSS_DOC_NUMBER")
  private String docNumber;

  @Column(name = "GCSS_ROUTE")
  private String route;

  @Column(name = "GCSS_NIIN")
  private String niin;

  @Column(name = "GCSS_UI")
  private String ui;

  @Column(name = "GCSS_QTY")
  private Integer qty;

  @Column(name = "GCSS_CC")
  private String cc;

  @Column(name = "GCSS_DATE")
  private OffsetDateTime date;

  @Column(name = "PRIORITY")
  private String priority;

  @Column(name = "TRANSACTION_TYPE")
  private String transactionType;
}
