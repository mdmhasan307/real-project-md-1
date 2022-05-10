package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.common.domain.model.SortOrderPair;
import mil.usmc.mls2.stratis.common.model.enumeration.SearchTypeEnum;
import mil.usmc.mls2.stratis.core.domain.model.PackingConsolidationSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.PackingConsolidationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QEquipmentEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QPackingConsolidationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QPackingStationEntity;
import mil.usmc.mls2.stratis.core.infrastructure.util.BooleanExpressionBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.*;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class PackingConsolidationEntityRepositoryCustomImpl implements PackingConsolidationEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Long count(PackingConsolidationSearchCriteria criteria) {
    val qEntity = QPackingConsolidationEntity.packingConsolidationEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    return query.fetchCount();
  }

  @Override
  public List<PackingConsolidationEntity> search(PackingConsolidationSearchCriteria criteria) {
    val qEntity = QPackingConsolidationEntity.packingConsolidationEntity;
    val query = selectFrom(qEntity, entityManager);
    configurePredicate(query, createSearchPredicate(criteria, qEntity));
    configureOrderBy(query, createSortPairs(criteria, qEntity));
    configureLimits(query, criteria);
    return query.fetch();
  }

  @Override
  public Optional<PackingConsolidationEntity> findPackingConsolidationByEquipmentAndCustomer(String equipmentDescription, String packingGroup, Integer customerId) {
    val qEntity = QPackingConsolidationEntity.packingConsolidationEntity;
    val qEquipmentEntity = QEquipmentEntity.equipmentEntity;
    val qPackingStation = QPackingStationEntity.packingStationEntity;

    val query = selectFrom(qEntity, entityManager)
        .leftJoin(qPackingStation).on(qEntity.packingStationId.eq(qPackingStation.packingStationId))
        .leftJoin(qEquipmentEntity).on(qPackingStation.equipmentNumber.eq(qEquipmentEntity.equipmentNumber));
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.customer().customerId, customerId, expressions);
    match(qEquipmentEntity.packingGroup, packingGroup, expressions);
    match(qEquipmentEntity.description, equipmentDescription, expressions);
    configureOrderBy(query, generateSortOrderPairs("totalIssues", SortOrder.ASC, qPackingStation));

    return Optional.ofNullable(query.fetchOne());
  }

  private Predicate createSearchPredicate(PackingConsolidationSearchCriteria criteria, QPackingConsolidationEntity qEntity) {
    val expressions = BooleanExpressionBuilder.of(SearchTypeEnum.AND);
    match(qEntity.packLevel, criteria.getLevel(), expressions);
    match(qEntity.packColumn, criteria.getColumn(), expressions);
    match(qEntity.packingConsolidationId, criteria.getPackingConsolidationId(), expressions);
    match(qEntity.packingStationId, criteria.getPackingStationId(), expressions);
    match(qEntity.closedCarton, criteria.getCloseCarton(), expressions);
    match(qEntity.customer().customerId, criteria.getCustomerId(), expressions);
    match(qEntity.issuePriorityGroup, criteria.getIssuePriorityGroup(), expressions);

    if (criteria.getNullBarcode() != null && criteria.getNullBarcode()) {
      expressions.append(qEntity.consolidationBarcode.isNull());
    }

    return expressions.getExpression();
  }

  private List<SortOrderPair> createSortPairs(PackingConsolidationSearchCriteria criteria, QPackingConsolidationEntity qEntity) {
    val sorts = generateSortOrderPairs(criteria.getSortColumn(), criteria.getSortOrder(), qEntity);
    val filteredSorts = new ArrayList<SortOrderPair>(sorts.size());

    for (SortOrderPair sort : sorts) {
      String sortColumn = sort.getProperty();
      sort.setPath(qEntity);
      sort.setProperty(sortColumn);

      filteredSorts.add(sort);
    }

    return filteredSorts;
  }
}
