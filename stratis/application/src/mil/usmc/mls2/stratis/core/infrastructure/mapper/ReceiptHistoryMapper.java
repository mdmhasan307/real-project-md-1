package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistory;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ReceiptHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class ReceiptHistoryMapper {

  public ReceiptHistory map(ReceiptHistoryEntity input) {
    if (input == null) return null;

    return ReceiptHistory.builder()
        .rcn(input.getRcn())
        .frustrateCode(input.getFrustrateCode())
        .frustrateLocation(input.getFrustrateLocation())
        .quantityStowed(input.getQuantityStowed())
        .quantityReleased(input.getQuantityReleased())
        .quantityMeasured(input.getQuantityMeasured())
        .quantityInvoiced(input.getQuantityInvoiced())
        .quantityInduced(input.getQuantityInduced())
        .niinId(input.getNiinId())
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
        .userId(input.getUserId())
        .transaction(input.getTransaction())
        .timestamp(input.getTimestamp())
        .ri(input.getRi())
        .packingConsolidationId(input.getPackingConsolidationId())
        .cube(input.getCube())
        .oldRcn(input.getOldRcn())
        .niin(input.getNiin())
        .partialShipment(input.getPartialShipment())
        .qtyStowloss(input.getQtyStowloss())
        .overageFlag(input.getOverageFlag())
        .shortageFlag(input.getShortageFlag()).build();
  }
}
