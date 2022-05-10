package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "ref_ui")
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RefUiEntity implements Serializable {

  @Id
  @Column(name = "REF_UI_ID")
  private Integer id;

  @Column(name = "ui_conv_from")
  private String uiFrom;

  @Column(name = "ui_conv_to")
  private String uiTo;

  @Column(name = "conversion_factor")
  private Double conversionFactor;
}
