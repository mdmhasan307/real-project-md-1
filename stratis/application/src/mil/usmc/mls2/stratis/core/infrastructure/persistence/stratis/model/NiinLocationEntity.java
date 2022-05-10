package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "niin_location")
@DynamicInsert
@EqualsAndHashCode(of = "niinLocationId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class NiinLocationEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
*/
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "niin_location_generator")
  @SequenceGenerator(name = "niin_location_generator", sequenceName = "niin_loc_id_seq", allocationSize = 1)
  @Column(name = "NIIN_LOC_ID")
  private Integer niinLocationId;

  @ManyToOne
  @JoinColumn(name = "NIIN_ID")
  private NiinInfoEntity niinInfo;

  @ManyToOne
  @JoinColumn(name = "LOCATION_ID")
  private LocationEntity location;

  @Column(name = "QTY")
  private Integer qty;

  @Column(name = "EXPIRATION_DATE")
  private LocalDate expirationDate;

  @Column(name = "DATE_OF_MANUFACTURE")
  private LocalDate dateOfManufacture;

  @Column(name = "CC")
  private String cc;

  @Column(name = "LOCKED")
  private String locked;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "PROJECT_CODE")
  private String projectCode;

  @Column(name = "PC")
  private String pc;

  @Column(name = "LAST_INV_DATE")
  private OffsetDateTime lastInvDate;

  @Column(name = "CASE_WEIGHT_QTY")
  private Integer caseWeightQty;

  @Column(name = "LOT_CON_NUM")
  private String lotConNum;

  @Column(name = "SERIAL_NUMBER")
  private String serialNumber;

  @Column(name = "PACKED_DATE")
  private LocalDate packedDate;

  @Column(name = "NUM_EXTENTS")
  private Integer numExtents;

  @Column(name = "NUM_COUNTS")
  private Integer numCounts;

  @Column(name = "UNDER_AUDIT")
  private String underAudit;

  @Column(name = "OLD_UI")
  private String oldUi;

  @Column(name = "NSN_REMARK")
  private String nsnRemark;

  @Column(name = "EXP_REMARK")
  private String expRemark;

  @Column(name = "OLD_QTY")
  private Integer oldQty;

  @Column(name = "RECALL_FLAG")
  private String recallFlag;

  @Column(name = "RECALL_DATE")
  private OffsetDateTime recallDate;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
