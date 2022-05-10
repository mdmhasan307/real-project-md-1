package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "niin_info")
@EqualsAndHashCode(of = "niinId")
@Builder
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class NiinInfoEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "niin_info_generator")
  @SequenceGenerator(name = "niin_info_generator", sequenceName = "niin_id_seq", allocationSize = 1)
  @Column(name = "NIIN_ID")
  private Integer niinId;

  @Column(name = "NIIN")
  private String niin;

  @Column(name = "NOMENCLATURE")
  private String nomenclature;

  @Column(name = "CUBE")
  private BigDecimal cube;

  @Column(name = "PRICE")
  private int price;

  @Column(name = "ACTIVITY_DATE")
  private OffsetDateTime activityDate;

  @Column(name = "TAMCN")
  private String tamcn;

  @Column(name = "SUPPLY_CLASS")
  private String supplyClass;

  @Column(name = "TYPE_OF_MATERIAL")
  private String typeOfMaterial;

  @Column(name = "COGNIZANCE_CODE")
  private String cognizanceCode;

  @Column(name = "PART_NUMBER")
  private String partNumber;

  @Column(name = "UI")
  private String ui;

  @Column(name = "CAGE_CODE")
  private String cageCode;

  @Column(name = "FSC")
  private String fsc;

  @ManyToOne
  @JoinColumn(name = "SHELF_LIFE_CODE", referencedColumnName = "SHELF_LIFE_CODE")
  private RefSlcEntity shelfLifeCode;

  @Column(name = "WEIGHT")
  private BigDecimal weight;

  @Column(name = "LENGTH")
  private BigDecimal length;

  @Column(name = "WIDTH")
  private BigDecimal width;

  @Column(name = "HEIGHT")
  private BigDecimal height;

  @Column(name = "SHELF_LIFE_EXTENSION")
  private Integer shelfLifeExtension;

  @Column(name = "SCC")
  private String scc;

  @Column(name = "INVENTORY_THRESHOLD")
  private String inventoryThreshold;

  @Column(name = "SASSY_BALANCE")
  private Integer sassyBalance;

  @Column(name = "RO_THRESHOLD")
  private Integer roThreshold;

  @Column(name = "SMIC")
  private String smic;

  @Column(name = "SERIAL_CONTROL_FLAG")
  private String serialControlFlag;

  @Column(name = "LOT_CONTROL_FLAG")
  private String lotControlFlag;

  @Column(name = "NEW_NSN")
  private String newNsn;

  @Column(name = "CREATED_BY")
  private Integer createdBy;

  @Column(name = "MODIFIED_BY")
  private Integer modifiedBy;

  @Column(name = "LAST_MHIF_UPDATE_DATE")
  private OffsetDateTime lastMhifUpdateDate;

  @Column(name = "DEMIL_CODE")
  private String demilCode;

  @Column(name = "SECURITY_MARK_CLASS")
  private String securityMarkClass;
}
