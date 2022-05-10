package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHist;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingManifestHistEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ShippingManifestHistMapper {

  private final CustomerMapper customerMapper;

  public ShippingManifestHistMapper(@Lazy CustomerMapper customerMapper) {
    this.customerMapper = customerMapper;
  }

  public ShippingManifestHist map(ShippingManifestHistEntity input) {
    if (input == null) return null;

    return ShippingManifestHist.builder()
        .shippingManifestId(input.getShippingManifestId())
        .customer(customerMapper.map(input.getCustomer()))
        .leadTcn(input.getLeadTcn())
        .manifest(input.getManifest())
        .manifestDate(input.getManifestDate())
        .pickedUpDate(input.getPickedUpDate())
        .pickedUpFlag(input.getPickedUpFlag())
        .deliveredDate(input.getDeliveredDate())
        .deliveredFlag(input.getDeliveredFlag())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .manifestPrintDate(input.getManifestPrintDate())
        .driver(input.getDriver())
        .modeOfShipment(input.getModeOfShipment())
        .userId(input.getUserId())
        .transaction(input.getTransaction())
        .timestamp(input.getTimestamp())
        .equipmentNumber(input.getEquipmentNumber())
        .floorLocation(input.getFloorLocation())
        .building(input.getBuilding())
        .manifestedBy(input.getManifestedBy())
        .build();
  }
}
