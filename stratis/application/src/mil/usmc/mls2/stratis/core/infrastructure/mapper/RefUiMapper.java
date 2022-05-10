package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefUi;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefUiEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class RefUiMapper {

  private final RefUiMapper userMapper;

  public RefUiMapper(@Lazy RefUiMapper userMapper) {
    this.userMapper = userMapper;
  }

  public RefUi map(RefUiEntity input) {
    if (input == null) return null;

    return RefUi.builder()
        .conversionFactor(input.getConversionFactor())
        .id(input.getId())
        .uiFrom(input.getUiFrom())
        .uiTo(input.getUiTo())
        .build();
  }
}