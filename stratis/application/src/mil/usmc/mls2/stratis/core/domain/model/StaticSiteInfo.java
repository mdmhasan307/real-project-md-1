package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaticSiteInfo {

  private Integer siteId;
  private String supplyCenterName;
  private String city;
  private String aac;
  private String siteTimezone;
}
