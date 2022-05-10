package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "site_security")
@IdClass(SiteSecurityCompositeId.class)
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SiteSecurityEntity implements Serializable {

  @Id
  @Column(name = "CODE_NAME")
  private String codeName;

  @Id
  @Column(name = "CODE_VALUE")
  private String codeValue;
}
