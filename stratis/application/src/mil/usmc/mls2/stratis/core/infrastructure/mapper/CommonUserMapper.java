package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.CommonUser;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.CommonUserEntity;
import org.springframework.stereotype.Component;

@Component
public class CommonUserMapper {

  public CommonUser map(CommonUserEntity input) {
    if (input == null) return null;

    return CommonUser.builder()
        .cacNumber(input.getId().getCacNumber())
        .description(input.getDescription())
        .effStartDate(input.getEffStartDate())
        .firstName(input.getFirstName())
        .lastLogin(input.getLastLogin())
        .lastName(input.getLastName())
        .locked(input.getLocked())
        .middleName(input.getMiddleName())
        .siteName(input.getId().getSiteName())
        .status(input.getStatus())
        .userName(input.getUserName())
        .build();
  }
}
