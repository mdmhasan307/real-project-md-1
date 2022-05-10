package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.FloorLocation;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.FloorLocationEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class FloorLocationMapper {

  private final FloorLocationMapper floorLocationMapper;
  private final WarehouseMapper warehouseMapper;
  private final RouteMapper routeMapper;

  public FloorLocationMapper(@Lazy FloorLocationMapper floorLocationMapper, @Lazy WarehouseMapper warehouseMapper, @Lazy RouteMapper routeMapper) {
    this.floorLocationMapper = floorLocationMapper;
    this.warehouseMapper = warehouseMapper;
    this.routeMapper = routeMapper;
  }

  public FloorLocation map(FloorLocationEntity input) {
    if (input == null) return null;

    return FloorLocation.builder()
        .floorLocation(input.getFloorLocation())
        .floorLocationId(input.getFloorLocationId())
        .inUse(input.getInUse())
        .route(routeMapper.map(input.getRoute()))
        .warehouse(warehouseMapper.map(input.getWarehouse()))
        .build();
  }
}