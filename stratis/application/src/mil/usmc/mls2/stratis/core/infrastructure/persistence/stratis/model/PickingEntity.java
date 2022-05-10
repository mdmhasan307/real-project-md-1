package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "picking")
@EqualsAndHashCode(of = "pid")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PickingEntity implements Serializable {

  /*
Note: allocationSize is required to be set to 1 for Hibernate to use an internal Oracle Sequence correctly, if that sequence is used outside of
hibernate as well.
 */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "picking_generator")
  @SequenceGenerator(name = "picking_generator", sequenceName = "pid_seq", allocationSize = 1)
  @Column(name = "PID")
  private Integer pid;

  @Column(name = "SCN")
  private String scn;

  @Column(name = "SUFFIX_CODE")
  private String suffixCode;

  @Column(name = "PACKING_CONSOLIDATION_ID")
  private Integer packingConsolidationId;

  @ManyToOne
  @JoinColumn(name = "NIIN_LOC_ID")
  private NiinLocationEntity niinLocation;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "QTY_PICKED")
  private Integer qtyPicked;

  @Column(name = "PIN")
  private String pin;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "PICK_QTY")
  private Integer pickQty;

  @Column(name = "BYPASS_COUNT")
  private Integer bypassCount;

  @Column(name = "TIME_OF_PICK")
  private OffsetDateTime timeOfPick;

  @Column(name = "ASSIGN_TO_USER")
  private Integer assignToUser;

  @Column(name = "PICKED_BY")
  private Integer pickedBy;

  @Column(name = "PACKED_DATE")
  private OffsetDateTime packedDate;

  @Column(name = "REFUSED_BY")
  private Integer refusedBy;

  @Column(name = "REFUSED_DATE")
  private OffsetDateTime refusedDate;

  @Column(name = "QTY_REFUSED")
  private Integer qtyRefused;

  @Column(name = "REFUSED_FLAG")
  private String refusedFlag;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
