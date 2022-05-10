package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.SiteSecurity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteSecurityEntity;
import org.springframework.stereotype.Component;

@Component
public class SiteSecurityMapper {

  public SiteSecurity map(SiteSecurityEntity input) {
    if (input == null) return null;

    return SiteSecurity.builder()
        .codeName(input.getCodeName())
        .codeValue(input.getCodeValue()).build();
  }
}
