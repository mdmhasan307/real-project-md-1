package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorMapper {

  public Error map(ErrorEntity input) {
    if (input == null) return null;

    return Error.builder()
        .code(input.getCode())
        .description(input.getDescription())
        .id(input.getId())
        .securityLevel(input.getSecurityLevel())
        .label(input.getLabel())
        .title(input.getTitle())
        .build();
  }
}
