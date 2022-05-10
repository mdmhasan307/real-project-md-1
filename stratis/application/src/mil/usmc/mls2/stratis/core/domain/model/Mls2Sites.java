package mil.usmc.mls2.stratis.core.domain.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * MLS2_SITES tables. used to get connection pool information for active sites.
 */

@Getter
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mls2Sites {

  private UUID siteId;
  private String siteName;
  private String description;
  private String pdbName;
  private String pdbUsername;
  private String pdbPassword;
  private SiteStatusEnum status;
  private String flywayUsername;
  private String flywayPassword;
  private String flywaySchema;
  private String sortOrder;

  //for a site to be valid all pdb properties, and flyway properties must be set.
  public boolean isValidSite() {
    if (status != SiteStatusEnum.ACTIVE) return false;
    if (pdbName == null ||
        pdbUsername == null ||
        pdbPassword == null ||
        flywayUsername == null ||
        flywayPassword == null ||
        flywaySchema == null) return false;
    return true;
  }
}




