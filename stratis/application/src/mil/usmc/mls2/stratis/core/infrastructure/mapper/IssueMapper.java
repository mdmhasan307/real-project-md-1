package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Issue;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.IssueEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class IssueMapper {

  private final IssueMapper issueMapper;
  private final CustomerMapper customerMapper;

  public IssueMapper(@Lazy IssueMapper issueMapper, @Lazy CustomerMapper customerMapper) {
    this.issueMapper = issueMapper;
    this.customerMapper = customerMapper;
  }

  public Issue map(IssueEntity input) {
    if (input == null) return null;

    return Issue.builder()
        .adviceCode(input.getAdviceCode())
        .cc(input.getCc())
        .costJon(input.getCostJon())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .customer(customerMapper.map(input.getCustomer()))
        .dateBackOrdered(input.getDateBackOrdered())
        .demilCode(input.getDemilCode())
        .disposalCode(input.getDisposalCode())
        .distributionCode(input.getDistributionCode())
        .documentId(input.getDocumentId())
        .documentNumber(input.getDocumentNumber())
        .eroNumber(input.getEroNumber())
        .fundCode(input.getFundCode())
        .issuePriorityDesignator(input.getIssuePriorityDesignator())
        .issuePriorityGroup(input.getIssuePriorityGroup())
        .issueType(input.getIssueType())
        .mediaAndStatusCode(input.getMediaAndStatusCode())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .niinId(input.getNiinId())
        .packingConsolidationId(input.getPackingConsolidationId())
        .projectCode(input.getProjectCode())
        .qty(input.getQty())
        .qtyIssued(input.getQtyIssued())
        .rcn(input.getRcn())
        .rdd(input.getRdd())
        .relToShippingBy(input.getRelToShippingBy())
        .relToShippingDate(input.getRelToShippingDate())
        .ron(input.getRon())
        .routingIdFrom(input.getRoutingIdFrom())
        .scn(input.getScn())
        .securityMarkClass(input.getSecurityMarkClass())
        .signalCode(input.getSignalCode())
        .status(input.getStatus())
        .suffix(input.getSuffix())
        .supplementaryAddress(input.getSupplementaryAddress())
        .build();
  }
}