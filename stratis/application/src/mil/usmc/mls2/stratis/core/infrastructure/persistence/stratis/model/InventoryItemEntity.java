package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "INVENTORY_ITEM")
@EqualsAndHashCode(of = "inventoryItemId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class InventoryItemEntity implements Serializable {

  @Id
  @Column(name = "INVENTORY_ITEM_ID")
  private Integer inventoryItemId;

  @Column(name = "NIIN_ID")
  private Integer niinId;

  @ManyToOne
  @JoinColumn(name = "NIIN_LOC_ID")
  private NiinLocationEntity niinLocation;

  @Column(name = "INVENTORY_ID")
  private Integer inventoryId;

  @Column(name = "NUM_COUNTS")
  private Integer numCounts;

  @Column(name = "CUM_NEG_ADJ")
  private Integer cumNegAdj;

  @Column(name = "CUM_POS_ADJ")
  private Integer cumPosAdj;

  @Column(name = "NUM_COUNTED")
  private Integer numCounted;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "MODIFIED_DATE")
  private OffsetDateTime modifiedDate;

  @Column(name = "INV_TYPE")
  private String invType;

  @ManyToOne
  @JoinColumn(name = "WAC_ID")
  private WacEntity wac;

  @Column(name = "PRIORITY")
  private String priority;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "TRANSACTION_TYPE")
  private String transactionType;

  @Column(name = "ASSIGN_TO_USER")
  private Integer assignToUser;

  @Column(name = "BYPASS_COUNT")
  private Integer bypassCount;

  @Column(name = "NIIN_LOC_QTY")
  private Integer niinLocQty;

  @Column(name = "COMPLETED_BY")
  private Integer completedBy;

  @Column(name = "COMPLETED_DATE")
  private OffsetDateTime completedDate;

  @ManyToOne
  @JoinColumn(name = "LOCATION_ID")
  private LocationEntity location;

  @Column(name = "RELEASED_BY")
  private Integer releasedBy;

  @Column(name = "RELEASED_DATE")
  private OffsetDateTime releasedDate;
}
