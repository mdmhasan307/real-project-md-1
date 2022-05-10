package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Entity
@Table(name = "ref_slc")
@EqualsAndHashCode(of = "refSlcId")
@Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class RefSlcEntity implements Serializable {

  @Id
  @Column(name = "REF_SLC_ID")
  private Integer refSlcId;

  @Column(name = "SHELF_LIFE_CODE")
  private String shelfLifeCode;

  @Column(name = "SPAN")
  private Integer span;

  @Column(name = "EPXEND_LIFE_IN_A")
  private Integer epxendLifeInA;

  @Column(name = "EXPEND_LIFE_IN_B")
  private Integer expendLifeInB;

  @Column(name = "EXPEND_LIFE_IN_C")
  private Integer expendLifeInC;

  @Column(name = "REPAIR_LIFE_IN_A")
  private Integer repairLifeInA;

  @Column(name = "SPAN_DAYS")
  private Integer spanDays;

  @Column(name = "MIN_V_SPAN")
  private Integer minVSpan;

  @Column(name = "MIN_D_SPAN")
  private Integer minDSpan;
}
