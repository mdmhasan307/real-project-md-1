package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.InventoryItemEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class InventoryItemMapper {

  private final WacMapper wacMapper;
  private final LocationMapper locationMapper;
  private final NiinLocationMapper niinLocationMapper;

  public InventoryItemMapper(@Lazy WacMapper wacMapper, @Lazy NiinLocationMapper niinLocationMapper, @Lazy LocationMapper locationMapper) {
    this.wacMapper = wacMapper;
    this.niinLocationMapper = niinLocationMapper;
    this.locationMapper = locationMapper;
  }

  public InventoryItem map(InventoryItemEntity input) {
    if (input == null) return null;

    return InventoryItem.builder()
        .assignToUser(input.getAssignToUser())
        .bypassCount(input.getBypassCount())
        .completedBy(input.getCompletedBy())
        .completedDate(input.getCompletedDate())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .cumNegAdj(input.getCumNegAdj())
        .cumPosAdj(input.getCumPosAdj())
        .inventoryId(input.getInventoryId())
        .inventoryItemId(input.getInventoryItemId())
        .invType(input.getInvType())
        .location(locationMapper.map(input.getLocation()))
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .niinId(input.getNiinId())
        .niinLocation(niinLocationMapper.map(input.getNiinLocation()))
        .niinLocQty(input.getNiinLocQty())
        .numCounted(input.getNumCounted())
        .numCounts(input.getNumCounts())
        .priority(input.getPriority())
        .releasedBy(input.getReleasedBy())
        .releasedDate(input.getReleasedDate())
        .status(input.getStatus())
        .transactionType(input.getTransactionType())
        .wac(wacMapper.map(input.getWac()))
        .build();
  }
}