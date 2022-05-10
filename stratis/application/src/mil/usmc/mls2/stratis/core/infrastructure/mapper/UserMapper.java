package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.User;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.UserEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class UserMapper {

  private final UserMapper userMapper;

  public UserMapper(@Lazy UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public User map(UserEntity input) {
    if (input == null) return null;

    return User.builder()
        .userId(input.getUserId())
        .username(input.getUsername())
        .cacNumber(input.getCacNumber())
        .build();
  }
}