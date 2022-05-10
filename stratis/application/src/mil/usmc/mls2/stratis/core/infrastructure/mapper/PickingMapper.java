package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PickingEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class PickingMapper {

  private final NiinLocationMapper niinLocationMapper;

  public PickingMapper(@Lazy NiinLocationMapper niinLocationMapper) {
    this.niinLocationMapper = niinLocationMapper;
  }

  public Picking map(PickingEntity input) {
    if (input == null) return null;

    return Picking.builder()
        .assignToUser(input.getAssignToUser())
        .bypassCount(input.getBypassCount())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .niinLocation(niinLocationMapper.map(input.getNiinLocation()))
        .packedDate(input.getPackedDate())
        .packingConsolidationId(input.getPackingConsolidationId())
        .pickedBy(input.getPickedBy())
        .pickQty(input.getPickQty())
        .pid(input.getPid())
        .pin(input.getPin())
        .qtyPicked(input.getQtyPicked())
        .qtyRefused(input.getQtyRefused())
        .refusedBy(input.getRefusedBy())
        .refusedDate(input.getRefusedDate())
        .refusedFlag(input.getRefusedFlag())
        .status(input.getStatus())
        .scn(input.getScn())
        .suffixCode(input.getSuffixCode())
        .timeOfPick(input.getTimeOfPick())
        .securityMarkClass(input.getSecurityMarkClass())
        .build();
  }
}