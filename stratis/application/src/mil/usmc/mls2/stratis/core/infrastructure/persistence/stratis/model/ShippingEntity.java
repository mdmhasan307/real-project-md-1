package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "shipping")
@EqualsAndHashCode(of = "shippingId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShippingEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipping_generator")
  @SequenceGenerator(name = "shipping_generator", sequenceName = "shipping_id_seq", allocationSize = 1)
  @Column(name = "shipping_id")
  private Integer shippingId;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "QUANTITY")
  private Integer qty;

  @Column(name = "TCN")
  private String tcn;

  @ManyToOne
  @JoinColumn(name = "SHIPPING_MANIFEST_ID")
  private ShippingManifestEntity shippingManifest;

  @Column(name = "SCN")
  private String scn;

  @ManyToOne
  @JoinColumn(name = "PACKING_CONSOLIDATION_ID")
  private PackingConsolidationEntity packingConsolidation;

  @Column(name = "LAST_REVIEW_DATE")
  private OffsetDateTime lastReviewDate;

  @Column(name = "CALL_NUMBER")
  private String callNumber;

  @Column(name = "SHIPMENT_NUMBER")
  private String shipmentNumber;

  @Column(name = "TAILGATE_DATE")
  private OffsetDateTime tailgateDate;

  @Column(name = "LINE_NUMBER")
  private String lineNumber;

  @Column(name = "BILLED_AMOUNT")
  private Integer billedAmount;

  @Column(name = "EQUIPMENT_NUMBER")
  private Integer equipmentNumber;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
