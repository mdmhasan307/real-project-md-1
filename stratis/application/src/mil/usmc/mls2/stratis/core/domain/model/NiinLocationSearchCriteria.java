package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mil.usmc.mls2.stratis.common.domain.model.BaseSearchCriteria;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class NiinLocationSearchCriteria extends BaseSearchCriteria {

  private Integer locationId;
  private Integer niinId;
  private Boolean niinIdMatch;
  private String niin;
  private String cc;
  private Integer niinInventoryId;
  private Integer wacId;
  private String nsnRemark;
  private Boolean checkNumExtentsNull;
  private String expRemark;
  private LocalDate expirationDate;
  private List<String> sides;
  private String locked;
  private Boolean qtyGreaterThenZero;
}
