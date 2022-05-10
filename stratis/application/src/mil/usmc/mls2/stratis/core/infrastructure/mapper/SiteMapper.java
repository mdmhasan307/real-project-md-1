package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Site;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SiteEntity;
import org.springframework.stereotype.Component;

@Component
public class SiteMapper {

  public Site map(SiteEntity input) {
    if (input == null) return null;

    return Site.builder()
        .siteName(input.getSiteName())
        .description(input.getDescription())
        .status(input.getStatus())
        .localTime(input.getLocalTime())
        .build();
  }
}
