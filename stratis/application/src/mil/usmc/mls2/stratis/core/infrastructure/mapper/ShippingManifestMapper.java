package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifest;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingManifestEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class ShippingManifestMapper {

  private final CustomerMapper customerMapper;
  private final FloorLocationMapper floorLocationMapper;

  public ShippingManifestMapper(@Lazy CustomerMapper customerMapper, @Lazy FloorLocationMapper floorLocationMapper) {
    this.customerMapper = customerMapper;
    this.floorLocationMapper = floorLocationMapper;
  }

  public ShippingManifest map(ShippingManifestEntity input) {
    if (input == null) return null;

    return ShippingManifest.builder()
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .customer(customerMapper.map(input.getCustomer()))
        .deliveredDate(input.getDeliveredDate())
        .deliveredFlag(input.getDeliveredFlag())
        .driver(input.getDriver())
        .equipmentNumber(input.getEquipmentNumber())
        .floorLocation(floorLocationMapper.map(input.getFloorLocation()))
        .leadTcn(input.getLeadTcn())
        .manifest(input.getManifest())
        .manifestDate(input.getManifestDate())
        .manifestedBy(input.getManifestedBy())
        .manifestPrintDate(input.getManifestPrintDate())
        .modeOfShipment(input.getModeOfShipment())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .pickedUpDate(input.getPickedUpDate())
        .pickedUpFlag(input.getPickedUpFlag())
        .shippingManifestId(input.getShippingManifestId())
        .build();
  }
}