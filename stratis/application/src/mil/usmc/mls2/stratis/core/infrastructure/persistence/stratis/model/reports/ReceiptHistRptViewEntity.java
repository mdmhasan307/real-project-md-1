package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity
@Immutable
@Table(name = "V_RECEIPTS_HIST_RPT")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReceiptHistRptViewEntity {

  @EmbeddedId
  @AttributeOverrides({
      @AttributeOverride(name = "rcn", column = @Column(name = "RCN")),
      @AttributeOverride(name = "sid", column = @Column(name = "SID"))
  })
  private ReceiptHistRptKey id;

  @Column(name = "NIIN")
  private String niin;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime dateReceived;

  @Column(name = "DOCUMENT_NUMBER")
  private String documentNumber;

  @Column(name = "QUANTITY_INDUCTED")
  private Integer qtyReceived;

  @Column(name = "QUANTITY_STOWED")
  private Integer stowedQty;

  @Column(name = "QUANTITY_BACKORDERED")
  private Integer qtyBackordered;

  @Column(name = "LOCATION_LABEL")
  private String locationLabel;

  @Column(name = "CREATED_BY")
  private String receivedBy;

  @Column(name = "STOWED_BY")
  private String stowedBy;

  @Column(name = "STOWED_DATE")
  private OffsetDateTime stowedDate;
}
