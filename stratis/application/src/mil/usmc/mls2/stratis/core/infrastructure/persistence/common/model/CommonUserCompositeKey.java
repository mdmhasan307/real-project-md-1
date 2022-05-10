package mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class CommonUserCompositeKey implements Serializable {

  private String siteName;

  private String cacNumber;
}
