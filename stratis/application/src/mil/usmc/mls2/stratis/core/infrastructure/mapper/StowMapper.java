package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Stow;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.StowEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class StowMapper {

  private final StowMapper locationMapper;
  private final LocationMapper locMapper;
  private final PickingMapper pickingMapper;
  private final ReceiptMapper receiptMapper;

  public StowMapper(@Lazy StowMapper locationMapper, @Lazy LocationMapper locMapper, @Lazy PickingMapper pickingMapper, @Lazy ReceiptMapper receiptMapper) {
    this.locationMapper = locationMapper;
    this.locMapper = locMapper;
    this.pickingMapper = pickingMapper;
    this.receiptMapper = receiptMapper;
  }

  public Stow map(StowEntity input) {
    if (input == null) return null;

    return Stow.builder()
        .assignToUser(input.getAssignToUser())
        .bypassCount(input.getBypassCount())
        .cancelReason(input.getCancelReason())
        .caseWeightQty(input.getCaseWeightQty())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .dateOfManufacture(input.getDateOfManufacture())
        .expirationDate(input.getExpirationDate())
        .location(locMapper.map(input.getLocation()))
        .lotConNum(input.getLotConNum())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .pick(pickingMapper.map(input.getPickingEntity()))
        .packedDate(input.getPackedDate())
        .qtyToBeStowed(input.getQtyToBeStowed())
        .receipt(receiptMapper.map(input.getReceipt()))
        .scanInd(input.getScanInd())
        .securityMarkClass(input.getSecurityMarkClass())
        .serialNumber(input.getSerialNumber())
        .sid(input.getSid())
        .status(input.getStatus())
        .stowedBy(input.getStowedBy())
        .stowedDate(input.getStowedDate())
        .stowId(input.getStowId())
        .stowQty(input.getStowQty())
        .build();
  }
}
