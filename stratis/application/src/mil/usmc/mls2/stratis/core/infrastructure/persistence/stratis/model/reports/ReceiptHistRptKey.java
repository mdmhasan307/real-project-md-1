package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.reports;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ReceiptHistRptKey implements Serializable {

  private Integer rcn;
  private String sid;
}
