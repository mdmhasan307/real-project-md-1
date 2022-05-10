package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.EquipmentEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class EquipmentMapper {

  private final EquipmentMapper equipmentMapper;
  private final WacMapper wacMapper;

  public EquipmentMapper(@Lazy EquipmentMapper equipmentMapper, @Lazy WacMapper wacMapper) {
    this.equipmentMapper = equipmentMapper;
    this.wacMapper = wacMapper;
  }

  public Equipment map(EquipmentEntity input) {
    if (input == null) return null;

    return Equipment.builder()
        .comPortEquipmentId(input.getComPortEquipmentId())
        .comPortPrinterId(input.getComPortPrinterId())
        .currentUserId(input.getCurrentUserId())
        .description(input.getDescription())
        .equipmentNumber(input.getEquipmentNumber())
        .name(input.getName())
        .wac(wacMapper.map(input.getWac()))
        .warehouseId(input.getWarehouseId())
        .hasCubiscan(input.getHasCubiscan())
        .packingGroup(input.getPackingGroup())
        .printerName(input.getPrinterName())
        .shippingArea(input.getShippingArea())
        .build();
  }
}