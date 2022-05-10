package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "stow")
@EqualsAndHashCode(of = "stowId")
@Builder
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StowEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stow_id_generator")
  @SequenceGenerator(name = "stow_id_generator", sequenceName = "stow_id_seq", allocationSize = 1)
  @Column(name = "STOW_ID")
  private Integer stowId;

  @Column(name = "SID")
  private String sid;

  @Column(name = "QTY_TO_BE_STOWED")
  private int qtyToBeStowed;

  @ManyToOne
  @JoinColumn(name = "RCN")
  private ReceiptEntity receipt;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @ManyToOne
  @JoinColumn(name = "PID")
  private PickingEntity pickingEntity;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "CANCEL_REASON")
  private String cancelReason;

  @Column(name = "EXPIRATION_DATE")
  private LocalDate expirationDate;

  @Column(name = "DATE_OF_MANUFACTURE")
  private LocalDate dateOfManufacture;

  @ManyToOne
  @JoinColumn(name = "LOCATION_ID")
  private LocationEntity location;

  @Column(name = "LOT_CON_NUM")
  private String lotConNum;

  @Column(name = "CASE_WEIGHT_QTY")
  private Integer caseWeightQty;

  @Column(name = "PACKED_DATE")
  private LocalDate packedDate;

  @Column(name = "SERIAL_NUMBER")
  private String serialNumber;

  @Column(name = "STOW_QTY")
  private Integer stowQty;

  @Column(name = "SCAN_IND")
  private String scanInd;

  @Column(name = "BYPASS_COUNT")
  private Integer bypassCount;

  @Column(name = "ASSIGN_TO_USER")
  private Integer assignToUser;

  @Column(name = "STOWED_BY")
  private Integer stowedBy;

  @Column(name = "STOWED_DATE")
  private OffsetDateTime stowedDate;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
