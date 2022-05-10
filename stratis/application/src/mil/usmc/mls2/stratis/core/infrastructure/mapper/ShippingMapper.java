package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Shipping;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class ShippingMapper {

  private final PackingConsolidationMapper packingConsolidationMapper;
  private final ShippingManifestMapper shippingManifestMapper;

  public ShippingMapper(@Lazy PackingConsolidationMapper packingConsolidationMapper, @Lazy ShippingManifestMapper shippingManifestMapper) {
    this.packingConsolidationMapper = packingConsolidationMapper;
    this.shippingManifestMapper = shippingManifestMapper;
  }

  public Shipping map(ShippingEntity input) {
    if (input == null) return null;

    return Shipping.builder()
        .billedAmount(input.getBilledAmount())
        .callNumber(input.getCallNumber())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .equipmentNumber(input.getEquipmentNumber())
        .lastReviewDate(input.getLastReviewDate())
        .lineNumber(input.getLineNumber())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .packingConsolidation(packingConsolidationMapper.map(input.getPackingConsolidation()))
        .qty(input.getQty())
        .scn(input.getScn())
        .securityMarkClass(input.getSecurityMarkClass())
        .shipmentNumber(input.getShipmentNumber())
        .shippingId(input.getShippingId())
        .shippingManifest(shippingManifestMapper.map(input.getShippingManifest()))
        .tailgateDate(input.getTailgateDate())
        .tcn(input.getTcn())
        .build();
  }
}