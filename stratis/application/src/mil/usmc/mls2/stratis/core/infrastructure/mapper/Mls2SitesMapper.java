package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Mls2Sites;
import mil.usmc.mls2.stratis.core.domain.model.SiteStatusEnum;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.Mls2SitesEntity;
import org.springframework.stereotype.Component;

@Component
public class Mls2SitesMapper {

  public Mls2Sites map(Mls2SitesEntity input) {
    if (input == null) return null;

    return Mls2Sites.builder()
        .siteName(input.getSiteName())
        .description(input.getDescription())
        .status(SiteStatusEnum.getType(input.getStatusId()))
        .pdbName(input.getPdbName())
        .pdbPassword(input.getPdbPassword())
        .pdbUsername(input.getPdbUsername())
        .siteId(input.getSiteId())
        .flywayPassword(input.getPdbPassword())
        .flywayUsername(input.getFlywayUsername())
        .flywaySchema(input.getFlywaySchema())
        .sortOrder(input.getSortOrder())
        .build();
  }
}
