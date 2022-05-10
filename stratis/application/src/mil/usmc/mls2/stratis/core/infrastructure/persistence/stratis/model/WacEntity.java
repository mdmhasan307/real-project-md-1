package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "WAC")
@EqualsAndHashCode(of = "wacId")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class WacEntity implements Serializable {

  @Id
  @Column(name = "WAC_ID")
  private int wacId;

  @Column(name = "TASKS_PER_TRIP")
  private Integer tasksPerTrip;

  @Column(name = "SIDS_PER_TRIP")
  private Integer sidsPerTrip;

  @Column(name = "WAREHOUSE_ID")
  private Integer warehouseId;

  @Column(name = "WAC_NUMBER")
  private String wacNumber;

  @Column(name = "MECHANIZED_FLAG")
  private String mechanizedFlag;

  @Column(name = "SECURE_FLAG")
  private String secureFlag;

  @Column(name = "BULK_AREA_NUMBER")
  private String bulkAreaNumber;

  @Column(name = "CAROUSEL_NUMBER")
  private String carouselNumber;

  @Column(name = "CAROUSEL_CONTROLLER")
  private Integer carouselController;

  @Column(name = "CAROUSEL_OFFSET")
  private Integer carouselOffset;

  @Column(name = "CAROUSEL_MODEL")
  private String carouselModel;

  @Column(name = "WAC_ORDER")
  private Integer wacOrder;

  @Column(name = "PACK_AREA")
  private String packArea;
}
