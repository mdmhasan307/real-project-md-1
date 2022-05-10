package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Warehouse;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.WarehouseEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class WarehouseMapper {

  private final WarehouseMapper warehouseMapper;

  public WarehouseMapper(@Lazy WarehouseMapper warehouseMapper) {
    this.warehouseMapper = warehouseMapper;
  }

  public Warehouse map(WarehouseEntity input) {
    if (input == null) return null;

    return Warehouse.builder()
        .building(input.getBuilding())
        .carrierName(input.getCarrierName())
        .complex(input.getComplex())
        .description(input.getDescription())
        .locationDeliveryPrefix(input.getLocationDeliveryPrefix())
        .locationDeliverySuffix(input.getLocationDeliverySuffix())
        .siteId(input.getSiteId())
        .warehouseId(input.getWarehouseId())
        .build();
  }
}