package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "packing_consolidation")
@EqualsAndHashCode(of = "packingConsolidationId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PackingConsolidationEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "packing_consolidation_generator")
  @SequenceGenerator(name = "packing_consolidation_generator", sequenceName = "packing_consolidation_id_seq", allocationSize = 1)
  @Column(name = "PACKING_CONSOLIDATION_ID")
  private Integer packingConsolidationId;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "CONSOLIDATION_CUBE")
  private BigDecimal consolidationCube;

  @Column(name = "CONSOLIDATION_WEIGHT")
  private BigDecimal consolidationWeight;

  @Column(name = "CLOSE_CARTON")
  private String closedCarton;

  @Column(name = "PACK_LOCATION_BARCODE")
  private String packLocationBarcode;

  @Column(name = "PACKING_STATION_ID")
  private Integer packingStationId;

  @Column(name = "PACK_COLUMN")
  private Integer packColumn;

  @Column(name = "PACK_LEVEL")
  private Integer packLevel;

  @Column(name = "NUMBER_OF_ISSUES")
  private Integer numberOfIssues;

  @ManyToOne
  @JoinColumn(name = "CUSTOMER_ID")
  private CustomerEntity customer;

  @Column(name = "CONSOLIDATION_BARCODE")
  private String consolidationBarcode;

  @Column(name = "PARTIAL_RELEASE")
  private String partialRelease;

  @Column(name = "ISSUE_PRIORITY_GROUP")
  private String issuePriorityGroup;

  @Column(name = "PACKED_BY")
  private Integer packedBy;

  @Column(name = "PACKED_DATE")
  private OffsetDateTime packedDate;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
