package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SiteSecurityCompositeId implements Serializable {

  private String codeName;
  private String codeValue;
}
