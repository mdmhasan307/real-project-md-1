package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.WacEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class WacMapper {

  private final WacMapper wacMapper;

  public WacMapper(@Lazy WacMapper wacMapper) {
    this.wacMapper = wacMapper;
  }

  public Wac map(WacEntity input) {
    if (input == null) return null;

    return Wac.builder()
        .bulkAreaNumber(input.getBulkAreaNumber())
        .carouselController(input.getCarouselController())
        .carouselModel(input.getCarouselModel())
        .carouselNumber(input.getCarouselNumber())
        .carouselOffset(input.getCarouselOffset())
        .mechanizedFlag(input.getMechanizedFlag())
        .packArea(input.getPackArea())
        .secureFlag(input.getSecureFlag())
        .sidsPerTrip(input.getSidsPerTrip())
        .tasksPerTrip(input.getTasksPerTrip())
        .wacId(input.getWacId())
        .wacNumber(input.getWacNumber())
        .wacOrder(input.getWacOrder())
        .warehouseId(input.getWarehouseId())
        .build();
  }
}