package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import lombok.NoArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptEntity;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class ReceiptMapper {

  private static final NiinInfoMapper NIIN_INFO_MAPPER = NiinInfoMapper.INSTANCE;
  
  public Receipt map(ReceiptEntity input) {
    if (input == null) return null;

    return Receipt.builder()
        .rcn(input.getRcn())
        .frustrateCode(input.getFrustrateCode())
        .frustrateLocation(input.getFrustrateLocation())
        .quantityStowed(input.getQuantityStowed())
        .quantityReleased(input.getQuantityReleased())
        .quantityMeasured(input.getQuantityMeasured())
        .quantityInvoiced(input.getQuantityInvoiced())
        .quantityInduced(input.getQuantityInduced())
        .niinInfo(NIIN_INFO_MAPPER.entityToModel(input.getNiinInfo()))
        .contactNumber(input.getContactNumber())
        .fundCode(input.getFundCode())
        .signalCode(input.getSignalCode())
        .documentNumber(input.getDocumentNumber())
        .documentID(input.getDocumentId())
        .status(input.getStatus())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .conversionFlag(input.getConversionFlag())
        .routingId(input.getRoutingId())
        .workStation(input.getWorkStation())
        .rdd(input.getRdd())
        .supplementaryAddress(input.getSupplementaryAddress())
        .consignee(input.getConsignee())
        .dodDistCode(input.getDodDistCode())
        .rpd(input.getRpd())
        .partialShipmentIndicator(input.getPartialShipmentIndicator())
        .tracabilityNumber(input.getTracabilityNumber())
        .classCommoditNumber(input.getClassCommoditNumber())
        .shipedFrom(input.getShipedFrom())
        .cc(input.getCc())
        .projectCode(input.getProjectCode())
        .pc(input.getPc())
        .cognizanceCode(input.getCognizanceCode())
        .mechNonMechFlag(input.getMechNonMechFlag())
        .ration(input.getRation())
        .suffix(input.getSuffix())
        .shelfLifeCode(input.getShelfLifeCode())
        .weight(input.getWeight())
        .length(input.getLength())
        .height(input.getHeight())
        .width(input.getWidth())
        .ui(input.getUi())
        .price(input.getPrice())
        .fsc(input.getFsc())
        .partNumber(input.getPartNumber())
        .serialNumber(input.getSerialNumber())
        .quantityBackordered(input.getQuantityBackordered())
        .ri(input.getRi())
        .packingConsolidationId(input.getPackingConsolidationId())
        .cube(input.getCube())
        .niin(input.getNiin())
        .partialShipment(input.getPartialShipment())
        .qtyStowloss(input.getQtyStowloss())
        .overageFlag(input.getOverageFlag())
        .shortageFlag(input.getShortageFlag())
        .securityMarkClass(input.getSecurityMarkClass()).build();
  }
}
