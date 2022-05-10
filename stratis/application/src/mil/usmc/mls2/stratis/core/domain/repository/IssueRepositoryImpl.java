package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Issue;
import mil.usmc.mls2.stratis.core.domain.model.IssueSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.IssueMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.IssueEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.CustomerEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.IssueEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class IssueRepositoryImpl implements IssueRepository {

  private final IssueEntityRepository entityRepository;
  private final CustomerEntityRepository customerEntityRepository;
  private final IssueMapper mapper;

  @Override
  public void save(Issue issue) {
    IssueEntity entity;
    if (issue.getScn() == null) {
      entity = new IssueEntity();
    }
    else {
      entity = entityRepository.findById(issue.getScn()).orElseGet(IssueEntity::new);
    }
    apply(entity, issue);
    entityRepository.save(entity);
  }

  @Override
  public Optional<Issue> findById(String scn) {
    return entityRepository.findById(scn).map(mapper::map);
  }

  @Override
  public Long count(IssueSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<Issue> search(IssueSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public void delete(Issue issue) {
    entityRepository.deleteById(issue.getScn());
  }

  @Override
  public void deleteByScn(String scn) {
    entityRepository.deleteById(scn);
  }

  @Override
  public void deleteHistoryByScnAndDate(String scn, OffsetDateTime createdDate) {
    entityRepository.deleteHistoryByScnAndDate(scn, createdDate);
  }

  @Override
  public long findCountOfBackOrdersForGbofProcessing(String documentNumber) {
    return entityRepository.findCountOfBackOrdersForGbofProcessing(documentNumber);
  }

  @Override
  public long getHistoryCount(String docNumber, String suffix, String issueType) {
    return entityRepository.getHistoryCount(docNumber, suffix, issueType);
  }

  @Override
  public Integer getNextIssueSequence() {
    return entityRepository.getNextIssueSequence();
  }

  private void apply(IssueEntity entity, Issue input) {
    entity.setAdviceCode(input.getAdviceCode());
    entity.setCc(input.getCc());
    entity.setCostJon(input.getCostJon());
    entity.setCreatedBy(input.getCreatedBy());
    entity.setCreatedDate(input.getCreatedDate());

    if (input.getCustomer() != null) {
      val customerEntity = customerEntityRepository.findById(input.getCustomer().getCustomerId()).orElseThrow(() -> new StratisRuntimeException(String.format("Unable to locate Customer with id '%s'", input.getCustomer().getCustomerId())));
      entity.setCustomer(customerEntity);
    }

    entity.setDateBackOrdered(input.getDateBackOrdered());
    entity.setDemilCode(input.getDemilCode());
    entity.setDisposalCode(input.getDisposalCode());
    entity.setDistributionCode(input.getDistributionCode());
    entity.setDocumentId(input.getDocumentId());
    entity.setDocumentNumber(input.getDocumentNumber());
    entity.setEroNumber(input.getEroNumber());
    entity.setFundCode(input.getFundCode());
    entity.setIssuePriorityDesignator(input.getIssuePriorityDesignator());
    entity.setIssuePriorityGroup(input.getIssuePriorityGroup());
    entity.setIssueType(input.getIssueType());
    entity.setMediaAndStatusCode(input.getMediaAndStatusCode());
    entity.setModifiedBy(input.getModifiedBy());
    entity.setModifiedDate(input.getModifiedDate());
    entity.setNiinId(input.getNiinId());
    entity.setPackingConsolidationId(input.getPackingConsolidationId());
    entity.setProjectCode(input.getProjectCode());
    entity.setQty(input.getQty());
    entity.setQtyIssued(input.getQtyIssued());
    entity.setRcn(input.getRcn());
    entity.setRdd(input.getRdd());
    entity.setRelToShippingBy(input.getRelToShippingBy());
    entity.setRelToShippingDate(input.getRelToShippingDate());
    entity.setRon(input.getRon());
    entity.setRoutingIdFrom(input.getRoutingIdFrom());
    entity.setScn(input.getScn());
    entity.setSecurityMarkClass(input.getSecurityMarkClass());
    entity.setSignalCode(input.getSignalCode());
    entity.setStatus(input.getStatus());
    entity.setSuffix(input.getSuffix());
    entity.setSupplementaryAddress(input.getSupplementaryAddress());
  }
}