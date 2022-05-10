package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.UserType;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserTypeEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class UserTypeMapper {

  private final UserTypeMapper userTypeMapper;

  public UserTypeMapper(@Lazy UserTypeMapper userTypeMapper) {
    this.userTypeMapper = userTypeMapper;
  }

  public UserType map(UserTypeEntity input) {
    if (input == null) return null;

    return UserType.builder()
        .description(input.getDescription())
        .timestamp(input.getTimestamp())
        .type(input.getType())
        .updatedBy(input.getUpdatedBy())
        .userTypeId(input.getUserTypeId())
        .build();
  }
}