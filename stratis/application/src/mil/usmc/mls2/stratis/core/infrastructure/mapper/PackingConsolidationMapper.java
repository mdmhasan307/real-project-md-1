package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidation;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PackingConsolidationEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class PackingConsolidationMapper {

  private final CustomerMapper customerMapper;

  public PackingConsolidationMapper(@Lazy CustomerMapper customerMapper) {
    this.customerMapper = customerMapper;
  }

  public PackingConsolidation map(PackingConsolidationEntity input) {
    if (input == null) return null;

    return PackingConsolidation.builder()
        .closedCarton(input.getClosedCarton())
        .consolidationBarcode(input.getConsolidationBarcode())
        .consolidationCube(input.getConsolidationCube())
        .consolidationWeight(input.getConsolidationWeight())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .customer(customerMapper.map(input.getCustomer()))
        .issuePriorityGroup(input.getIssuePriorityGroup())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .numberOfIssues(input.getNumberOfIssues())
        .packColumn(input.getPackColumn())
        .packedBy(input.getPackedBy())
        .packedDate(input.getPackedDate())
        .packingConsolidationId(input.getPackingConsolidationId())
        .packingStationId(input.getPackingStationId())
        .packLevel(input.getPackLevel())
        .packLocationBarcode(input.getPackLocationBarcode())
        .partialRelease(input.getPartialRelease())
        .securityMarkClass(input.getSecurityMarkClass())
        .build();
  }
}